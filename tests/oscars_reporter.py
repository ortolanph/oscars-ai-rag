import csv
from dataclasses import dataclass, field
from typing import List
from jinja2 import Template

FIELDNAMES = [
    "year_film",
    "year_ceremony",
    "ceremony",
    "category",
    "canon_category",
    "name",
    "film",
    "winner"
]


@dataclass
class OscarEntry:
    year_film: int = field()
    year_ceremony: int = field()
    ceremony: int = field()
    category: str = field()
    canon_category: str = field()
    name: str = field()
    film: str = field(default=None)
    winner: bool = field(default=False)


@dataclass
class NomineeData:
    year_film: int = field()
    name: str = field(default=None)
    film: str = field(default=None)
    winner: bool = field(default=False)


@dataclass
class CategoryData:
    category: str = field()
    nominees: List[NomineeData] = field(default_factory=list)


@dataclass
class OscarReportData:
    ceremony: int = field()
    year_ceremony: int = field()
    categories: List[CategoryData] = field(default_factory=list)


if __name__ == '__main__':
    data = []

    with open('oscars.csv', newline='') as csvfile:
        reader = csv.DictReader(csvfile, fieldnames=FIELDNAMES)
        reader.__next__()

        for row in reader:
            data.append(OscarEntry(**row))

        csvfile.close()

    seasons = set(map(lambda x: (int(x.ceremony), int(x.year_ceremony)), data))
    seasons = sorted(list(seasons))

    report_data = []

    for season in seasons:
        season_data = list(filter(lambda x: season[0] == int(x.ceremony), data))
        category_list = set(map(lambda x: x.canon_category, season_data))

        oscar_data = OscarReportData(ceremony=season[0], year_ceremony=season[1], categories=[])
        categories = []

        print(f"{season[0]} ({season[1]})")
        for category in category_list:
            contenders = list(filter(lambda x: x.canon_category == category, season_data))

            oscar_category_data = CategoryData(category=category, nominees=[])
            nominee_data_list = []

            print(f"\t{category}")
            for contender in contenders:
                nominee_data_list.append(NomineeData(
                    year_film=contender.year_film,
                    name=contender.name,
                    film=contender.film,
                    winner=contender.winner,
                ))
                print(
                    f"\t\t[{'*' if contender.winner == "True" else ' '}] {contender.name} ({contender.film}/{contender.year_film})")

            oscar_category_data.nominees = nominee_data_list
            categories.append(oscar_category_data)

        print(" ")

        oscar_data.categories = categories
        report_data.append(oscar_data)

    with open("oscars_template.md", "r") as report_template:
        template = Template(
            report_template.read(),
            trim_blocks=True,
            lstrip_blocks=True
        )

    rendered_document = template.render(
        oscars=report_data,
    )

    with open(f'oscars.md', 'w') as report:
        report.writelines(rendered_document)

    with open("oscars_template.html", "r") as report_template:
        template = Template(
            report_template.read(),
            trim_blocks=True,
            lstrip_blocks=True
        )

    rendered_document = template.render(
        oscars=report_data,
    )

    with open(f'oscars.html', 'w') as report:
        report.writelines(rendered_document)

    print("--------------------------------------------------------------------------------")
