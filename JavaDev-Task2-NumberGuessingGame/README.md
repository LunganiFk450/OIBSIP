# Number Guessing Game – Java Swing GUI

## Project Overview

The Number Guessing Game is a desktop based application developed using Java Swing. The game challenges users to guess a randomly generated number within a specified range. After each guess, the application provides feedback indicating whether the guessed number is too high, too low, or correct.

The application features multiple difficulty levels, an attempt counter, score tracking, and an option to play multiple rounds.

This project was developed as part of the **Oasis Infobyte Java Development Internship (OIBSIP)**.

## Technologies Used

- **Programming Language:** Java
- **GUI Framework:** Java Swing
- **IDE:** Visual Studio Code
- **Java Libraries:**
  - `javax.swing.*` – Graphical user interface components.
  - `java.awt.*` – GUI layouts, colours, fonts and styling.
  - `java.util.Random` – Random number generation.

## Features

- **Random Number Generation:** The computer generates a random number at the beginning of each round.
- **Difficulty Levels:** Users can select from three difficulty levels.
- **Interactive GUI:** A graphical interface allows users to enter guesses and interact with the game.
- **Instant Feedback:** Displays "Too High!", "Too Low!", or "Correct!" depending on the user's guess.
- **Attempt Counter:** Tracks the number of attempts used during each round.
- **Maximum Attempts:** Limits the number of guesses depending on the selected difficulty.
- **Win and Loss Notifications:** Displays a success message or reveals the correct number when the user loses.
- **Score Tracking:** Keeps track of the total rounds played and successful guesses.
- **Play Again:** Allows users to start another round without restarting the application.
- **Input Validation:** Prevents invalid inputs and numbers outside the selected range.

## Difficulty Levels

| Difficulty | Number Range | Maximum Attempts |
|---|---|---|
| Easy | 1–50 | 10 |
| Medium | 1–100 | 7 |
| Hard | 1–200 | 5 |

## How to Run the Application

### Prerequisites

- Java Development Kit (JDK) installed.
- Visual Studio Code or another Java-compatible IDE.

### Step 1: Clone the Repository

```bash
git clone https://github.com/LunganiFk450/OIBSIP.git
```

### Step 2: Open the Project

Open the project folder in Visual Studio Code.

### Step 3: Compile the Java Program

Open the terminal in the project directory and execute:

```bash
Main.java
```

### Step 4: Run the Application

```bash
Main
```

The Number Guessing Game window will open.

## How to Play

1. Launch the application.
2. Select your preferred difficulty level.
3. Read the displayed number range.
4. Enter your guess in the input field.
5. Click the **GUESS NUMBER** button.
6. Follow the feedback provided:
   - **Too High!** – Enter a lower number.
   - **Too Low!** – Enter a higher number.
   - **Correct!** – You have successfully guessed the number.
7. Try to guess the number before running out of attempts.
8. Click **PLAY AGAIN** to start another round.
9. Track your performance using the round and wins counters.

## Core Java Concepts Applied

This project demonstrates the practical application of the following Java programming concepts:

- **Classes and Objects:** Used to structure the application and create GUI components.
- **Variables and Data Types:** Used to store the secret number, attempts, scores and difficulty settings.
- **Switch Statements:** Used to determine game settings based on the selected difficulty.
- **Loops and Conditional Statements:** Used to control game logic and evaluate guesses.
- **Event Handling:** Used to respond to button clicks and user interactions.
- **Exception Handling:** Used to handle invalid numerical input.
- **Random Number Generation:** Used to generate a different secret number for each round.
- **Object-Oriented Programming:** Used to organise the game into methods and manage application state.

## Learning Outcomes

Through this project, I gained practical experience in Java desktop application development, particularly:

- Developing graphical user interfaces using Java Swing.
- Implementing interactive application features.
- Applying conditional logic and switch statements.
- Handling user input and validation.
- Working with event-driven programming.
- Understanding random number generation and game logic.

## Author

**Lungani Fakazi**

Java Development Intern – Oasis Infobyte (OIBSIP)

## Acknowledgements

Thanks to **Oasis Infobyte** for providing the opportunity to develop practical Java programming skills through this internship project.