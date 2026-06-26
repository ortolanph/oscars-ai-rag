import csv
from dataclasses import dataclass, field
from collections import Counter
import matplotlib.pyplot as plt

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


if __name__ == '__main__':
    oscars_data = []

    with open('oscars.csv', 'r') as csvfile:
        reader = csv.DictReader(csvfile, fieldnames=FIELDNAMES)
        reader.__next__()

        for row in reader:
            # Convert winner field to boolean
            winner_value = row.get('winner', '').strip() if row.get('winner') else ''
            row['winner'] = winner_value.lower() in ('true', '1', 'yes')
            row['year_film'] = int(row['year_film'])
            row['year_ceremony'] = int(row['year_ceremony'])
            row['ceremony'] = int(row['ceremony'])
            oscars_data.append(OscarEntry(**row))

        csvfile.close()

    # Count entries for each ceremony
    ceremony_counts = Counter(entry.ceremony for entry in oscars_data)
    
    # Display results sorted by ceremony number
    print("Entries per ceremony:")
    for ceremony in sorted(ceremony_counts.keys()):
        print(f"Ceremony {ceremony}: {ceremony_counts[ceremony]} entries")
    
    print(f"\nTotal entries: {len(oscars_data)}")
    print(f"Total ceremonies: {len(ceremony_counts)}")
    
    # Calculate average entries per ceremony
    average_entries = len(oscars_data) / len(ceremony_counts) if ceremony_counts else 0
    print(f"Average entries per ceremony: {average_entries:.2f}")
    
    # Create graph: Nominees and Winners by Time (Year)
    # Group entries by year_ceremony
    year_counts = {}
    for entry in oscars_data:
        year = int(entry.year_ceremony)
        if year not in year_counts:
            year_counts[year] = {'nominees': 0, 'winners': 0}

        if entry.winner:
            year_counts[year]['winners'] += 1
        else:
            year_counts[year]['nominees'] += 1

    # Prepare data for plotting
    years = sorted(year_counts.keys())
    nominees_count = [year_counts[year]['nominees'] for year in years]
    winners_count = [year_counts[year]['winners'] for year in years]

    # Create the graph
    plt.figure(figsize=(14, 6))
    plt.plot(years, nominees_count, marker='o', label='Nominees', linewidth=2, markersize=4)
    plt.plot(years, winners_count, marker='s', label='Winners', linewidth=2, markersize=4)

    plt.xlabel('Year')
    plt.ylabel('Count')
    plt.title('Oscar Nominees and Winners by Year')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()

    # Save the graph to a file
    output_file = 'oscars_nominees_winners.png'
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"\nGraph saved to '{output_file}'")

    # Show the graph
    plt.show()


