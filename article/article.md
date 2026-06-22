# Oscars AI

**THIS FILE IS A DRAFT**

> Lights, Camera, and... Action

For a short period of time we are transported to a world created by the imagination of screenwriters, envisioned by
directors, acted by actresses and actors. Whether that period is five minutes or two and a half hours, we are not sit on
a movie theater, we are elsewhere, since escaping from a horde of monsters on a land of fantasy imagination land, or
even in a real city of the world in some historic moment. Pure magic.

But to transport us to another place, movies have a big team, with people taking care of the scenario, wardrobe, music,
sound effects, special effects, among others. There is a huge team of professionals doing their work in order to
transport us where we must be. When one thing is not done well, we feel uncomfortable and we remember that we are sit on
a movie theater. They must do their work well. They must be the best.

And to be recognized, there are movie festivals, where a small sample of the movies are selected during a certain period
of time (commonly one year). There is a lot of festivals nowadays and one of them
is [The Oscars](https://www.oscars.org/). It started small, in 1929, occurring at lunchtime, with 270 people, in a hotel
and delivered 15 statues for movies starred in 1927 and 1928. It evolved to become one of the most important ceremonies
of the world.

Many happened in the movie industry (and to The Oscars) from the first edition to 2025 (my dataset is from 1929 to
2025): the advent of sound in movies, the advent of colors in movies, war and other conflicts inspired people, people
and culture influenced moviemakers, books and other media (like comics) were important source of inspiration, a lot
happened since the first edition.

This article intention is to use RAG (Retrieval-Augmented Generation) with AI in Spring Boot AI project to create an
interface in natural language asking questions for The Oscars dataset. The Oscars has a huge amount of data that can be
discovered using those questions and not with an mechanic API offering some selected methods.

---

## The code

The [code for this article](https://github.com/ortolanph/oscars-ai-rag) is located at my Github.

## RAG - Retrieval-augmented Generation

This project uses a datasource in CSV format, ingests it into a Vector Database (in this
case [Redis](https://redis.io/)), exposes an endpoint to accept input in natural language, select the data from this
database, asks an AI API to compile the data with the prompt, and returns a response to the user.

The main magic here is to load (or ingest) the data in the database. AI alone can't extract anything from thin air. Even
if you ask in a client like [Chat GPT](https://chatgpt.com/) or [Google Gemini](https://gemini.google.com/), they will
perform a web search, assemble a small database, and work with their data.

But the problem is that companies have sensible data on which can not be exposed to public or processed in public use
AI. It's a huge security problem. Other problem is that ChatGPT and Google Gemini are generic clients, they have no
knowledge of the company's data. Mistakes can be done and response can not be satisfactory.

[Retrieval-augmented generation (RAG)](https://en.wikipedia.org/wiki/Retrieval-augmented_generation) solves this problem
by storing the data in a separate database as a side library for an AI client. There are two main components:

1. The Retriever: collect data from databases, documents or other knowledge sources (structured or unstructured) (
   audios, videos, web pages, images) to create a solid source of data
2. Generator: a model on which transforms the ingested data into a human-readable response according to the user prompt

## The Data

The data used on this article was extracted
from [Kaggle The Oscar Award, 1927 - 2026 dataset](https://www.kaggle.com/datasets/unanimad/the-oscar-award) that is
an online community for data science and machine learning. It's a Google subsidiary which serves as a central hub where
practitioners and enthusiasts can collaborate, share resources, and test their skills.

There are 11240 lines of data with all the Oscars information. This data is structured as a CSV file, so it'll be easy
to ingest with the right framework. It could be a document file (PDF) with styles to separate ceremonies, and the
categories.

## How it works

## Monitoring

## Some thoughts

Something is missing here. There are some questions that could not be answered like:

One thing that I could do is to break the ingestion of data by ceremony (ceremony field) and not by a number to divide
the data. This way I could extract some extra information like:

* The most nominated movie of the season
* The most winner movie of the season
* [The Big Five](https://en.wikipedia.org/wiki/List_of_Big_Five_Academy_Award_winners_and_nominees) movies (
  a movie that won the best movie, best director, best actor, best actress, and best screenplay adapted or original)
* Ranking of countries that won an Oscar in the International Feature Film category and their movies
* Many others...

This way, the similarity search could work better and add more layers to the example data for the AI take conclusions
for a better response.

## Conclusions

RAG is a powerful AI framework, but the data source and how it is loaded will interfere with the data processing. The
better the data is stored in the Vector Database, better the responses will get. Of course that, a specialized AI model
will return a more refined answer.