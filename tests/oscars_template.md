# Academy Awards - Oscars 

{% for oscar in oscars %}
## Oscars {{oscar.year_ceremony}} ({{oscar.ceremony}})

{% for category in oscar.categories %}
### Category {{category.category}}

{% for nominee in category.nominees %}
{% if nominee.winner == "True" %}
* **{{nominee.name}} ({{nominee.film}}/{{nominee.year_film}})**
{% else %}
* {{nominee.name}} ({{nominee.film}}/{{nominee.year_film}})
{% endif %}
{% endfor %}

{% endfor %}

{% endfor %}
