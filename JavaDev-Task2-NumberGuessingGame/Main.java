
import java.awt.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Main extends JFrame {

    private final Random random = new Random();

    private int secretNumber;
    private int attempts;
    private int maxAttempts;
    private int maxNumber;
    private int round = 1;
    private int wins = 0;

    private boolean gameOver = false;

    private JComboBox<String> difficultyBox;
    private JTextField guessField;
    private JButton guessButton;
    private JButton playAgainButton;

    private JLabel feedbackLabel;
    private JLabel attemptsLabel;
    private JLabel roundLabel;
    private JLabel winsLabel;
    private JLabel rangeLabel;

    private final Color backgroundColor = new Color(18, 24, 45);
    private final Color cardColor = new Color(31, 40, 68);
    private final Color accentColor = new Color(125, 92, 255);
    private final Color textColor = new Color(240, 242, 255);

    public Main() {

        setTitle("Number Guessing Game");
        setSize(520, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        createInterface();
        startNewRound();

    }

    private void createInterface() {

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("NUMBER GUESSING GAME");
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setForeground(textColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Test your luck and find the hidden number!");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(170, 180, 210));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(25));

        // Score panel
        JPanel scorePanel = new JPanel(new GridLayout(1, 2, 12, 0));
        scorePanel.setBackground(backgroundColor);
        scorePanel.setMaximumSize(new Dimension(450, 75));

        roundLabel = createScoreLabel("ROUND: 1");
        winsLabel = createScoreLabel("WINS: 0");

        scorePanel.add(roundLabel);
        scorePanel.add(winsLabel);

        mainPanel.add(scorePanel);
        mainPanel.add(Box.createVerticalStrut(22));

        // Game card
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        gamePanel.setBackground(cardColor);
        gamePanel.setBorder(new EmptyBorder(22, 22, 22, 22));

        // Difficulty
        JLabel difficultyLabel = createLabel("SELECT DIFFICULTY");

        difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        difficultyBox.setFont(new Font("Arial", Font.PLAIN, 14));
        difficultyBox.setMaximumSize(new Dimension(400, 38));
        difficultyBox.addActionListener(e -> startNewRound());

        // Range
        rangeLabel = createLabel("Guess a number between 1 and 100");

        // Input
        JLabel inputLabel = createLabel("ENTER YOUR GUESS");

        guessField = new JTextField();
        guessField.setFont(new Font("Arial", Font.BOLD, 22));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setMaximumSize(new Dimension(400, 48));
        guessField.setBackground(new Color(245, 246, 255));
        guessField.setForeground(Color.BLACK);

        // Guess button
        guessButton = new JButton("GUESS NUMBER");
        styleButton(guessButton);

        guessButton.addActionListener(e -> checkGuess());

        guessField.addActionListener(e -> checkGuess());

        // Feedback
        feedbackLabel = new JLabel(
                "I'm thinking of a number. Can you guess it?"
        );

        feedbackLabel.setFont(new Font("Arial", Font.BOLD, 14));
        feedbackLabel.setForeground(textColor);
        feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Attempts
        attemptsLabel = new JLabel("Attempts: 0 / 7");
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 15));
        attemptsLabel.setForeground(Color.WHITE);
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        gamePanel.add(difficultyLabel);
        gamePanel.add(Box.createVerticalStrut(8));
        gamePanel.add(difficultyBox);
        gamePanel.add(Box.createVerticalStrut(18));

        gamePanel.add(rangeLabel);
        rangeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gamePanel.add(Box.createVerticalStrut(22));
        gamePanel.add(inputLabel);
        gamePanel.add(Box.createVerticalStrut(8));
        gamePanel.add(guessField);
        gamePanel.add(Box.createVerticalStrut(15));

        gamePanel.add(guessButton);
        gamePanel.add(Box.createVerticalStrut(20));

        gamePanel.add(feedbackLabel);
        gamePanel.add(Box.createVerticalStrut(15));
        gamePanel.add(attemptsLabel);

        mainPanel.add(gamePanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Play Again button
        playAgainButton = new JButton("PLAY AGAIN");
        styleButton(playAgainButton);

        playAgainButton.setEnabled(false);
        playAgainButton.addActionListener(e -> {
            round++;
            startNewRound();
        });

        mainPanel.add(playAgainButton);

        // Footer
        JLabel footer = new JLabel("GOOD LUCK!");
        footer.setForeground(new Color(160, 170, 200));
        footer.setFont(new Font("Arial", Font.BOLD, 12));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalStrut(18));
        mainPanel.add(footer);

        add(mainPanel);
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(textColor);

        return label;
    }

    private JLabel createScoreLabel(String text) {

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 17));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(cardColor);

        return label;
    }

    private void styleButton(JButton button) {

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(accentColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(400, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void startNewRound() {

        String difficulty = (String) difficultyBox.getSelectedItem();
        switch (difficulty) {
            case "Easy" -> {
                maxNumber = 50;
                maxAttempts = 10;
            }
            case "Hard" -> {
                maxNumber = 200;
                maxAttempts = 5;
            }
            default -> {
                maxNumber = 100;
                maxAttempts = 7;
            }
        }

        secretNumber = random.nextInt(maxNumber) + 1;

        attempts = 0;
        gameOver = false;

        rangeLabel.setText(
                "Guess a number between 1 and " + maxNumber
        );

        feedbackLabel.setText("I'm thinking of a number. Can you guess it?");
        feedbackLabel.setForeground(textColor);

        attemptsLabel.setText("Attempts: 0 / " + maxAttempts);

        roundLabel.setText("ROUND: " + round);
        winsLabel.setText("WINS: " + wins);

        guessField.setText("");
        guessField.setEnabled(true);
        guessButton.setEnabled(true);
        playAgainButton.setEnabled(false);

        guessField.requestFocusInWindow();
    }

    private void checkGuess() {

        if (gameOver) {
            return;
        }

        String input = guessField.getText().trim();

        if (input.isEmpty()) {
            feedbackLabel.setText("Please enter a number!");
            feedbackLabel.setForeground(Color.ORANGE);
            return;
        }

        int guess;

        try {
            guess = Integer.parseInt(input);

        } catch (NumberFormatException e) {
            feedbackLabel.setText("Invalid input! Enter a whole number.");
            feedbackLabel.setForeground(Color.ORANGE);
            return;
        }

        if (guess < 1 || guess > maxNumber) {
            feedbackLabel.setText(
                    "Enter a number between 1 and " + maxNumber + "!"
            );
            feedbackLabel.setForeground(Color.ORANGE);
            return;
        }

        attempts++;

        attemptsLabel.setText(
                "Attempts: " + attempts + " / " + maxAttempts
        );

        if (guess > secretNumber) {

            feedbackLabel.setText("Too High! Try a lower number.");
            feedbackLabel.setForeground(new Color(255, 120, 120));

        } else if (guess < secretNumber) {

            feedbackLabel.setText("Too Low! Try a higher number.");
            feedbackLabel.setForeground(new Color(255, 200, 100));

        } else {

            feedbackLabel.setText("Correct! You guessed it!");
            feedbackLabel.setForeground(new Color(100, 255, 160));

            wins++;
            gameOver = true;

            endRound();
            return;
        }

        // Check maximum attempts
        if (attempts >= maxAttempts) {

            feedbackLabel.setText(
                    "You Lost! The number was " + secretNumber
            );

            feedbackLabel.setForeground(Color.RED);

            gameOver = true;
            endRound();
        }

        guessField.setText("");
        guessField.requestFocusInWindow();
    }

    private void endRound() {

        guessField.setEnabled(false);
        guessButton.setEnabled(false);
        playAgainButton.setEnabled(true);

        winsLabel.setText("WINS: " + wins);

        JOptionPane.showMessageDialog(this,"Round " + round + "\nAttempts used: " + attempts  + "\nTotal wins: " + wins,"Round Summary",JOptionPane.INFORMATION_MESSAGE);

    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Main game = new Main();
            game.setVisible(true);
        });

    }
}