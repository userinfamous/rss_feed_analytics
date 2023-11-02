package one;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;
import java.util.Set;

public class App {
    private static List<String> left_leaning_wordlists = Arrays.asList("progressives", "equality", "social", "justice", "healthcare", "climate", "environment", "police reform", "lgbtq+", "voting", "inequality", "minimum wage", "housing", "funding", "gun control", "women's", "racial", "immigration reform", "universal healthcare", "labor rights", "lgbtq+ equality", "environmental", "redistribution", "diversity", "inclusivity", "pro-choice", "affordable", "workers", "social", "reform", "renewable", "universal basic income", "criminal justice", "climate justice", "affordable childcare", "student debt relief", "green", "economic justice", "lgbtq+ rights", "gender equality", "medicare", "fair", "affordable", "feminism", "acceptance", "net neutrality", "clean water", "income tax", "sustainability", "public", "community", "paid", "immigration rights", "feminist", "should", "mental", "safety", "gay", "renewable", "reform", "rainbow", "pronouns", "marijuana", "drag", "gender", "green energy", "fair wages", "lgbtq+ equality", "universal pre-K", "police", "elder care", "environmental conservation", "income", "disability", "marriage", "gender", "diversity", "inclusion", "equal pay", "progressive", "social", "clean energy", "public transportation", "gender-neutral", "indigenous", "access", "benefits", "reproductive", "protection", "drug", "adoption", "internet", "legal", "care", "dental", "housing", "universal", "opportunity", "anti-discrimination laws", "representation", "rights"
    );
    private static List<String> right_leaning_wordlists = Arrays.asList("traditional", "limited", "personal", "free", "family", "national", "pro-life", "freedom", "second amendment", "individual", "lower taxes", "strong borders", "economic growth", "american exceptionalism", "deregulation", "law and order", "principles", "small government", "constitutional", "free speech", "traditional marriage", "fiscal conservatism", "military", "enterprise", "energy independence", "parental", "rights", "patriotism", "right to bear arms", "states' rights", "tax cuts", "border security", "religious", "defense", "capitalism", "freedom", "trade", "traditional culture", "pro-business", "entrepreneurship", "pro-Israel", "self-reliance", "sovereignty", "lower", "traditional education", "work", "ethic", "personal", "identity", "tough", "veterans", "pro-family", "anti-abortion", "gun", "individualism", "meritocracy", "values", "private", "limited welfare", "strong foreign policy", "American", "local", "reduced", "deregulation", "individual", "oil", "pride", "American exceptionalism", "exploration", "conservative", "strength", "economy", "leadership", "free-market", "coal", "power", "power plants", "china", "maui", "biden", "trump", "arms", "american flag", "american history", "amendment", "parental", "children", "border", "security", "limited entitlements", "businesses", "accountability", "defense spending", "unity", "grassroots", "anti-socialism", "anti-globalism", "pro-life", "veterans", "anti-communist", "strong stance", "stance", "equity"
    );

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        // Set up the frame
        JFrame frame = new JFrame("RSS Feed Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
    
        // URL input field
        JTextField urlField = new JTextField();
        urlField.setBorder(BorderFactory.createTitledBorder("Enter RSS feed URL"));
    
        // Text area for RSS feed output
        JTextArea outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
    
        // Text areas for displaying left and right leaning words
        JTextArea leftWordsArea = new JTextArea(5, 20);
        leftWordsArea.setEditable(false);
        JScrollPane leftWordsScrollPane = new JScrollPane(leftWordsArea);
    
        JTextArea rightWordsArea = new JTextArea(5, 20);
        rightWordsArea.setEditable(false);
        JScrollPane rightWordsScrollPane = new JScrollPane(rightWordsArea);

        // Spinner for entry limit
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 1000, 1); // Initial value, min, max, step
        JSpinner entryLimitSpinner = new JSpinner(spinnerModel);
        entryLimitSpinner.setBorder(BorderFactory.createTitledBorder("Entry Limit"));
    
        // Panel for the sentiment and leaning labels
        JPanel percentagePanel = new JPanel();
        percentagePanel.setLayout(new BoxLayout(percentagePanel, BoxLayout.Y_AXIS));
        JLabel sentimentLabel = new JLabel("<html><font size='5'>Avg Sentiment Rating:</font>0.00%</html>");
        JLabel leftLeanLabel = new JLabel("<html><font size='5'>Left-Leaning Word Percentage:</font>0.00%</html>");
        JLabel rightLeanLabel = new JLabel("<html><font size='5'>Right-Leaning Word Percentage:</font>0.00%</html>");
        JLabel trendLabel = new JLabel("<html><font size='5'>Trend:</font></html>");
        percentagePanel.add(sentimentLabel);
        percentagePanel.add(leftLeanLabel);
        percentagePanel.add(rightLeanLabel);
        percentagePanel.add(trendLabel);
    
        // Panel at the top containing the URL field and percentage panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(urlField, BorderLayout.NORTH);
        topPanel.add(percentagePanel, BorderLayout.CENTER);

        // Here we add the spinner to the top panel
        topPanel.add(entryLimitSpinner, BorderLayout.SOUTH);
    
        // Main panel with a border layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(leftWordsScrollPane, BorderLayout.WEST);
        mainPanel.add(rightWordsScrollPane, BorderLayout.EAST);
    
        // RSS buttons panel
        JPanel rssButtonPanel = new JPanel(new FlowLayout());
        JButton alJazeeraButton = new JButton("Al Jazeera All News");
        JButton nytWorldButton = new JButton("NYT World News");
        JButton wsjWorldButton = new JButton("WSJ World News");
        JButton foxWorldButton = new JButton("Fox World News");
        JButton cnnWorldButton = new JButton("CNN World News");
    
        // Add action listeners for buttons
        alJazeeraButton.addActionListener(e -> fetchAndDisplayFeed("https://www.aljazeera.com/xml/rss/all.xml", outputTextArea, leftLeanLabel, rightLeanLabel, trendLabel, sentimentLabel, leftWordsArea, rightWordsArea, (Integer) entryLimitSpinner.getValue()));
        nytWorldButton.addActionListener(e -> fetchAndDisplayFeed("https://rss.nytimes.com/services/xml/rss/nyt/World.xml", outputTextArea, leftLeanLabel, rightLeanLabel, trendLabel, sentimentLabel, leftWordsArea, rightWordsArea, (Integer) entryLimitSpinner.getValue()));
        wsjWorldButton.addActionListener(e -> fetchAndDisplayFeed("https://feeds.a.dj.com/rss/RSSWorldNews.xml", outputTextArea, leftLeanLabel, rightLeanLabel, trendLabel, sentimentLabel, leftWordsArea, rightWordsArea, (Integer) entryLimitSpinner.getValue()));
        foxWorldButton.addActionListener(e -> fetchAndDisplayFeed("https://feeds.foxnews.com/foxnews/world", outputTextArea, leftLeanLabel, rightLeanLabel, trendLabel, sentimentLabel, leftWordsArea, rightWordsArea, (Integer) entryLimitSpinner.getValue()));
        cnnWorldButton.addActionListener(e -> fetchAndDisplayFeed("http://rss.cnn.com/rss/edition_world.rss", outputTextArea, leftLeanLabel, rightLeanLabel, trendLabel, sentimentLabel, leftWordsArea, rightWordsArea, (Integer) entryLimitSpinner.getValue()));
    
        rssButtonPanel.add(alJazeeraButton);
        rssButtonPanel.add(nytWorldButton);
        rssButtonPanel.add(wsjWorldButton);
        rssButtonPanel.add(foxWorldButton);
        rssButtonPanel.add(cnnWorldButton);
    
        // Fetch button
        JButton fetchButton = new JButton("Fetch RSS Feed");
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userUrl = urlField.getText().trim();
                if (!userUrl.isEmpty()) {
                    outputTextArea.setText("");
                    // Here the spinner's value is retrieved and passed to the fetchAndDisplayFeed method
                    int entryLimitValue = (Integer) entryLimitSpinner.getValue();
                    fetchAndDisplayFeed(
                        userUrl,
                        outputTextArea,
                        leftLeanLabel,
                        rightLeanLabel,
                        trendLabel,
                        sentimentLabel,
                        leftWordsArea,
                        rightWordsArea,
                        entryLimitValue // The spinner's value is used here
                    );
                }
            }
        });
    
        // Adding components to the frame
        frame.getContentPane().add(rssButtonPanel, BorderLayout.NORTH);
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.getContentPane().add(fetchButton, BorderLayout.SOUTH);
    
        // Display the window
        frame.setVisible(true);
    }
    
    

    private static void fetchAndDisplayFeed(
    String userUrl,
    JTextArea outputTextArea,
    JLabel leftLeanLabel,
    JLabel rightLeanLabel,
    JLabel trendLabel,
    JLabel sentimentLabel,
    JTextArea leftWordsArea,
    JTextArea rightWordsArea,
    int entryLimit // Add entryLimit as a parameter
) {
    try {
        Document doc = Jsoup.connect(userUrl).get();
        Elements entries = doc.select("item");

        double totalSentimentScore = 0;
        int numEntries = 0;

        int leftLeanCount = 0;
        int rightLeanCount = 0;
        int totalWords = 0;

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Lists to store found words
        Set<String> foundLeftWords = new HashSet<>();
        Set<String> foundRightWords = new HashSet<>();

        for (Element entry : entries) {
            if (entryLimit <= 0) break;

            String entryTitle = entry.select("title").text();
            String entryLink = entry.select("link").text();
            String entryDescription = Jsoup.clean(entry.select("description").text(), Safelist.relaxed());

            Annotation annotation = new Annotation(entryDescription);
            pipeline.annotate(annotation);
            double sentimentScore = 0;

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                sentimentScore += RNNCoreAnnotations.getPredictedClass(sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class));
            }
            sentimentScore /= annotation.get(CoreAnnotations.SentencesAnnotation.class).size();

            totalSentimentScore += sentimentScore;
            numEntries++;

            String[] words = entryDescription.toLowerCase().split("\\s+");
            totalWords += words.length;
            for (String word : words) {
                if (left_leaning_wordlists.contains(word)) {
                    leftLeanCount++;
                    foundLeftWords.add(word);
                }
                if (right_leaning_wordlists.contains(word)) {
                    rightLeanCount++;
                    foundRightWords.add(word);
                }
            }

            // Output RSS feed entry details
            outputTextArea.append("Entry Title: " + entryTitle + "\n");
            outputTextArea.append("Entry Link: " + entryLink + "\n");
            outputTextArea.append("Entry Description: " + entryDescription + "\n");
            outputTextArea.append("Sentiment Score: " + sentimentScore + "\n");
            outputTextArea.append("-----------------------------\n");

            entryLimit--;
        }

        double averageSentiment = numEntries > 0 ? (totalSentimentScore / numEntries) : 0;
         // Update the sentiment label
         String sentimentResult = "";
         if (averageSentiment-2 > 0) {
             sentimentResult=" Positive";
         } else {
             sentimentResult=" Negative";
         };
         sentimentLabel.setText("<html><font size='5'>Avg Sentiment Rating: " + String.format("%.2f ", (averageSentiment-2)*100) + "%" + sentimentResult + "</font></html>");

        double leftLeanPercentage = (double) leftLeanCount / totalWords * 100;
        double rightLeanPercentage = (double) rightLeanCount / totalWords * 100;

        leftLeanLabel.setText("<html><font size='5'>Left-Leaning Word Percentage: " + String.format("%.2f", leftLeanPercentage) + "%</font></html>");
        rightLeanLabel.setText("<html><font size='5'>Right-Leaning Word Percentage: " + String.format("%.2f", rightLeanPercentage) + "%</font></html>");

        if (leftLeanPercentage > rightLeanPercentage) {
            trendLabel.setText("<html><font size='5'>Trend: Left-Leaning</font></html>");
        } else if (rightLeanPercentage > leftLeanPercentage) {
            trendLabel.setText("<html><font size='5'>Trend: Right-Leaning</font></html>");
        } else {
            trendLabel.setText("<html><font size='5'>Trend: Neutral</font></html>");
        }

        // Update the JTextAreas with the found words
        leftWordsArea.setText("Left Learning Words: "+String.join(", ", foundLeftWords));
        rightWordsArea.setText("Right Learning Words: "+String.join(", ", foundRightWords));

    } catch (IOException e) {
        outputTextArea.append("Error: " + e.getMessage() + "\n");
        e.printStackTrace(); // Print the full stack trace for debugging
    }
}
}


