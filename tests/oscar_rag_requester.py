import argparse
import csv
import random
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from urllib import response

import requests

BASE_URL = "http://localhost:10002/oscars/tellme/{username}/what={question}"
HEADERS = ["username", "delay", "modified_delay", "question", "answer"]
USERS = [
    {"username": "steven_spielberg", "delay": 10},
    {"username": "george_lucas", "delay": 15},
    {"username": "francis_ford_coppola", "delay": 20},
    {"username": "quentin_tarantino", "delay": 30},
    {"username": "akira_kurosawa", "delay": 50},
    {"username": "stanley_kubrick", "delay": 120},
    {"username": "david_lynch", "delay": 100},
    {"username": "ari_aster", "delay": 45},
    {"username": "martin_scorsese", "delay": 60},
    {"username": "ricardo_darin", "delay": 130},
    {"username": "agnes_varda", "delay": 30},
    {"username": "kathryn_bigelow", "delay": 15},
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


def perform_request(username, question):
    response = requests.get(
        BASE_URL.format(
            username=username,
            question=question
        )
    )

    if response.status_code != 200:
        return response.text
    else:
        response.raise_for_status()
        return None


def fetch_data(user_parameter):
    print(f"fetch_data({user_parameter})")

    modified_delay = random.randint(5, user_parameter['delay'])
    print(f"\tSleeping for {modified_delay}")
    time.sleep(modified_delay)
    random_question = random.choice(questions)

    result = {
        "username": user_parameter["username"],
        "delay": user_parameter["delay"],
        "modified_delay": modified_delay,
        "question": random_question.strip(),
    }

    try:
        request_counter.increment()
        answer = perform_request(user_parameter["username"], random_question)

        result.update({
            "answer": answer,
            "status_code": response.status_code,
        })

        return result
    except requests.exceptions.HTTPError as e:
        result.update({
            "answer": str(e),
            "status_code": response.status_code,
        })

        return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Perform simulations.")
    parser.add_argument("--d", "--dry-run", help="Dry run", required=False, default=False, action="store_true")

    args = parser.parse_args()
    dry_run = int(args.d)

    if dry_run:
        print("DRY RUN")
        random_user = random.choice(USERS)
        print(fetch_data(random_user))
        sys.exit(0)

    with open("questions.txt", "r") as file:
        questions = file.readlines()

    results = []

    start = time.time()
    with ThreadPoolExecutor(max_workers=10) as executor:
        for i in range(10):
            print(f"==== ROUND {i + 1} ====")
            with ThreadPoolExecutor(max_workers=4) as executor:
                results.extend(list(executor.map(fetch_data, USERS)))

    end = time.time()

    with open('oscars_queries.csv', 'w', newline='') as oscars_file:
        writer = csv.DictWriter(oscars_file, fieldnames=FIELDNAMES)

        writer.writeheader()
        writer.writerows(results)

    print("===========================================================================")
    print(f"Elapsed Time ........: {end - start}")
    print(f"Number of requests ..: {request_counter.value}")
