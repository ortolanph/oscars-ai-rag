import argparse
import csv
import random
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from urllib import response

import requests

BASE_URL = "http://localhost:10002/oscars/tellme/{username}?what={question}"
HEADERS = ["username", "delay", "modified_delay", "question", "answer"]
USERS = [
    {"username": "steven_spielberg", "delay": 20},
    {"username": "george_lucas", "delay": 15},
    {"username": "francis_ford_coppola", "delay": 20},
    {"username": "quentin_tarantino", "delay": 130},
    {"username": "akira_kurosawa", "delay": 50},
    {"username": "stanley_kubrick", "delay": 120},
    {"username": "david_lynch", "delay": 100},
    {"username": "ari_aster", "delay": 145},
    {"username": "martin_scorsese", "delay": 60},
    {"username": "ricardo_darin", "delay": 130},
    {"username": "agnes_varda", "delay": 30},
    {"username": "kathryn_bigelow", "delay": 115},
    {"username": "jane_campion", "delay": 20},
    {"username": "sofia_coppola", "delay": 40},
    {"username": "greta_gerwig", "delay": 30},
    {"username": "chloe_zhao", "delay": 150},
    {"username": "lina_wertmüller", "delay": 180},
    {"username": "mira_nair", "delay": 30},
    {"username": "celine_sciamma", "delay": 60},
    {"username": "sarah_polley", "delay": 30},
]
FIELDNAMES = ["username", "delay", "modified_delay", "question", "answer", "status_code"]

questions = [str]
number_of_requests: int = 0


class AtomicCounter:
    def __init__(self):
        self._value = 0
        self._lock = threading.Lock()

    def increment(self):
        with self._lock:
            self._value += 1

    @property
    def value(self):
        return self._value


request_counter = AtomicCounter()


def run_simulations(users):
    execution_results = []

    start_time = time.time()
    with ThreadPoolExecutor(max_workers=10) as executor:
        for i in range(10):
            print(f"==== ROUND {i + 1} ====")
            execution_results.extend(list(executor.map(fetch_data, users)))

    end_time = time.time()

    return execution_results, start_time, end_time


def perform_request(username, question):
    formatted_url = BASE_URL.format(
        username=username,
        question=question.strip()
    )

    my_response = requests.get(formatted_url)

    if my_response.status_code == 200:
        return my_response.text, my_response.status_code
    else:
        my_response.raise_for_status()
        return None, my_response.status_code


def fetch_data(user_parameter):
    print(f"fetch_data({user_parameter})")

    modified_delay = random.randint(5, user_parameter['delay'])
    print(f"\tSleeping for {modified_delay} seconds")
    time.sleep(modified_delay)
    random_question = random.choice(questions)
    print(f"\tQ: {random_question.strip()}")

    result = {
        "username": user_parameter["username"],
        "delay": user_parameter["delay"],
        "modified_delay": modified_delay,
        "question": random_question.strip(),
    }

    try:
        request_counter.increment()
        answer = perform_request(user_parameter["username"], random_question)

        print(f"\tA: {answer[0]}")

        result.update({
            "answer": answer[0],
            "status_code": answer[1],
        })

        return result
    except requests.exceptions.HTTPError as e:
        print("\tOw, snap! Something went wrong...")

        result.update({
            "answer": str(e),
            "status_code": e.response.status_code,
        })

        return result


def load_questions():
    global questions
    with open("questions.txt", "r") as file:
        questions = file.readlines()


def generate_csv(results):
    now = datetime.now()
    time_stamp = datetime.strftime(now, "%Y%m%d%H%M%S")
    with open(f'oscars_queries_{time_stamp}.csv', 'w', newline='') as oscars_file:
        writer = csv.DictWriter(oscars_file, fieldnames=FIELDNAMES)

        writer.writeheader()
        writer.writerows(results)


def print_report(start_time, end_time, number_of_requests):
    print("===========================================================================")
    print(f"Elapsed Time ........: {end_time - start_time}")
    print(f"Number of requests ..: {number_of_requests}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Perform simulations.")
    parser.add_argument("-d", "--dry-run", help="Dry run", required=False, default=False, action="store_true")

    args = parser.parse_args()
    dry_run = int(args.dry_run)

    load_questions()

    test_users = []

    if dry_run:
        print("DRY RUN")
        test_users.append(random.choice(USERS))
    else:
        test_users = USERS

    simulation_results = run_simulations(test_users)
    generate_csv(simulation_results[0])
    print_report(simulation_results[1], simulation_results[2], request_counter.value)
