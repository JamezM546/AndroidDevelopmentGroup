# Milestone 1 - Movie Recommender App (Unit 7)

## Table of Contents

1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)

## Overview

### Description

The Movie Recommender App helps users find movies they will enjoy by asking a few simple questions about their preferences (genre, mood, etc.). It uses a movie API to gather current movie data and an AI model to generate personalized recommendations based on the user’s answers.

### App Evaluation

[Evaluation of your app across the following attributes]
- **Category:** Entertainment / Recommendation
- **Mobile:** Mobile-first app with a simple question-based interface and results screen
- **Story:** A user wants to quickly find a movie to watch without scrolling endlessly. The app asks a few questions and gives a tailored recommendation.
- **Market:** Movie watchers, students, and casual users who want quick suggestions
- **Habit:** Can be used repeatedly whenever the user wants something new to watch
- **Scope:** Moderate — combines API usage, UI, and AI integration but remains achievable

## Product Spec

### 1. User Features (Required and Optional)

**Required Features**

1. User answers questions about movie preferences (genre, mood, etc.)
2. App fetches movie data from a movie API
3. AI generates a personalized movie recommendation
4. Display recommended movie with title, description, and rating
5. Show multiple recommendations instead of one

**Optional Features**

1. Rate movies to improve future recommendations
2. Sort by genres, rating, etc.
3. Show full movie details on a separate screen

### 2. Screen Archetypes

- Preference Quiz Screen
  - User opens the app and is prompted to answer a few simple questions about their movie preferences
  - User selects options such as genre, mood, or popularity level
  - User submits their answers to generate personalized recommendations
- Recommendation Results Screen
  - User views a scrollable list or grid of recommended movies based on their quiz responses
  - User can tap on any movie to see more details
  - User can continue browsing recommendations
- Movie Detail Screen
  - User taps on a movie from the recommendation list
  - User views detailed information such as poster, description, rating, and release year
  - User can choose to save the movie to their favorites or watchlist
- Favorites / Watchlist Screen
  - User navigates to a screen showing movies they previously saved
  - User can scroll through saved movies
  - User can tap a saved movie to view its details again

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Discover -> Question / Preference Screen
* Recommendations -> Result Screen
* Favorites -> Saved Movies Screen

**Flow Navigation** (Screen to Screen)

- Welcome / Home Screen
  - Leads to Questions / Preferences Screen
  - Can also go to Favorites Screen
- Question / Preferences Screen
  - User answers movie preference questions
  - Leads to Recommendations Screen after submission
- Recommendations Screen
  - Displays multiple personalized movie recommendations
  - User can tap a movie for more details
  - User can save a movie to Favorites
  - Can return to Question / Preferences Screen to try again
- Movie Details Screen
  - Shows extended movie description
  - User can save movie to Favorites
  - Can return to Recommendations Screen
- Favorites Screen
  - Displays saved movies
  - User can tap a saved movie to view details
  - Can return to Discover tab to get new recommendations


## Wireframes

![image alt](https://github.com/JamezM546/AndroidDevelopmentGroup/blob/e484efd4729c29ef3ded0ac032144fb2b41a53b6/images/wireframe.JPG)

<br>

<br>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

<br>

# Milestone 2 - Build Sprint 1 (Unit 8)

## GitHub Project board

[Add screenshot of your Project Board with three milestones visible in
this section]
![image alt](https://github.com/CS388FinalProject/AndroidDevelopmentGroup/blob/b5d535db66d3666750f9cac9b6979f14353a30f4/images/milestonesoverview.jpg)
![image alt](https://github.com/CS388FinalProject/AndroidDevelopmentGroup/blob/b5d535db66d3666750f9cac9b6979f14353a30f4/images/board.jpg)

## Issue cards

- [Add screenshot of your Project Board with the issues that you've been working on for this unit's milestone] ![image alt](https://github.com/CS388FinalProject/AndroidDevelopmentGroup/blob/b5d535db66d3666750f9cac9b6979f14353a30f4/images/milestone1issues.jpg)
- [Add screenshot of your Project Board with the issues that you're working on in the **NEXT sprint**. It should include issues for next unit with assigned owners.]
- ![image alt](https://github.com/CS388FinalProject/AndroidDevelopmentGroup/blob/b5d535db66d3666750f9cac9b6979f14353a30f4/images/milestone2issues.jpg)

## Issues worked on this sprint
In this milestone, we worked on the following issues:

- Issue 1: Build Welcome and Personality Test Screen UI
- Issue 2: Set up navigation skeleton for all major screens
- Issue 3: Build Content Choice Screen
- Issue 4: Build Movie Preference Quiz Screen UI
- Issue 5: Build Book Preference Quiz Screen UI
- Issue 6: Create reusable shared quiz UI components
- Issue 7: Create shared loading, error, and empty-state components

![Milestone 1 Demo](images/milestone2.gif)

<br>

# Milestone 3 - Build Sprint 2 (Unit 9)

## GitHub Project board

[Add screenshot of your Project Board with the updated status of issues for Milestone 3. Note that these should include the updated issues you worked on for this sprint and not be a duplicate of Milestone 2 Project board.]

Here are the closed sprint 2 issues
![image alt](images/milestone3issues.png)

Here are the issues that are still open:
![image alt](images/milestone3issuesopen.png)

## Completed user stories

- Issue 8: Add personality answer state management
- Issue 9: Define personality-to-preference mapping rules
- Issue 10: Integrate personality data into recommendation input
- Issue 11: Build movie API integration
- Issue 12: Create movie recommendation filtering/ranking logic
- Issue 13: Add movie quiz state model and validation
- Issue 14: Build book API or dataset integration
- Issue 15: Create book recommendation filtering/ranking logic
- Issue 16: Define shared data models for movie and book results
- Issue 17: Build shared Recommendation Results screen
- Issue 18: Build reusable movie and book result cards
- Issue 19: Add navigation from results to detail screen
- Issue 20: Add validation behavior to personality, movie, and book quizzes
- Issue 21: Add loading and error handling to recommendation flow
- Issue 22: Add empty-state handling to results screen
- Pending user stories: None
- User stories cut from original requirements: None

[Add video/gif of your current application that shows build progress]
![Milestone 3 Demo](images/ms3.gif)

## App Demo Video

- Embed the YouTube/Vimeo link of your Completed Demo Day prep video
- [Demo Video](https://youtu.be/a8kjrN5mKHs?si=rWKVI7TFX9jC0Yal)
