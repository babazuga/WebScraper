package com.example.webscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

public class WebScraperUI extends JFrame {

    private JTextField urlField;
    private JButton scrapeButton, saveButton;
    private JCheckBox titleCheckbox, metaCheckbox, paragraphsCheckbox, linksCheckbox, imagesCheckbox;
    private JTextArea resultArea;
    private List<String> scrapedData;
    private List<String> imageUrls;
    private JProgressBar progressBar;

    public WebScraperUI() {
        setTitle("Web Scraper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        setResizable(true);
        setLocationRelativeTo(null);


        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveMenuItem = new JMenuItem("Save to CSV");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        saveMenuItem.addActionListener(e -> {
            if (scrapedData != null && !scrapedData.isEmpty()) {
                saveToCSV(scrapedData);
            } else {
                JOptionPane.showMessageDialog(WebScraperUI.this, "No data to save!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitMenuItem.addActionListener(e -> System.exit(0));

        fileMenu.add(saveMenuItem);
        fileMenu.add(exitMenuItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem howToUseMenuItem = new JMenuItem("How to Use");
        howToUseMenuItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Enter a URL and select the components you want to scrape. Click 'Scrape' to start.",
                        "How to Use",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        helpMenu.add(howToUseMenuItem);

        JMenu aboutMenu = new JMenu("About");
        JMenuItem aboutAppMenuItem = new JMenuItem("About This App");
        aboutAppMenuItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Web Scraper App\nMade by Gaurav\nVersion 1.0",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aboutMenu.add(aboutAppMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel urlPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JLabel urlLabel = new JLabel("URL:");
        urlField = new JTextField(40);
        scrapeButton = new JButton("Scrape");
        saveButton = new JButton("Save to CSV");

        urlPanel.add(urlLabel);
        urlPanel.add(urlField);
        urlPanel.add(scrapeButton);
        urlPanel.add(saveButton);


        JPanel optionsPanel = new JPanel(new GridLayout(1, 5));
        titleCheckbox = new JCheckBox("Title", true);
        metaCheckbox = new JCheckBox("Meta Description", true);
        paragraphsCheckbox = new JCheckBox("Paragraphs", true);
        linksCheckbox = new JCheckBox("Links", true);
        imagesCheckbox = new JCheckBox("Images");

        optionsPanel.add(titleCheckbox);
        optionsPanel.add(metaCheckbox);
        optionsPanel.add(paragraphsCheckbox);
        optionsPanel.add(linksCheckbox);
        optionsPanel.add(imagesCheckbox);

        topPanel.add(urlPanel, BorderLayout.NORTH);
        topPanel.add(optionsPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(scrollPane, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setString("Ready");
        progressBar.setStringPainted(true);

        add(progressBar, BorderLayout.SOUTH);

        scrapeButton.addActionListener(e -> {
            String url = urlField.getText().trim();
            if (!url.isEmpty()) {
                progressBar.setIndeterminate(true);
                progressBar.setString("Scraping...");
                new Thread(() -> {
                    scrapedData = scrapeWebPage(url);
                    imageUrls = scrapeImageUrls(url); // Get image URLs during scraping
                    updateResultArea(scrapedData);
                    progressBar.setIndeterminate(false);
                    progressBar.setString("Ready");
                }).start();
            } else {
                JOptionPane.showMessageDialog(WebScraperUI.this, "Please enter a valid URL.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        saveButton.addActionListener(e -> {
            if (scrapedData != null && !scrapedData.isEmpty()) {
                saveToCSV(scrapedData);
            } else {
                JOptionPane.showMessageDialog(WebScraperUI.this, "No data to save!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private List<String> scrapeWebPage(String url) {
        List<String> scrapedData = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();

            if (titleCheckbox.isSelected()) {
                scrapedData.add("Title: " + document.title());
            }

            if (metaCheckbox.isSelected()) {
                Elements metaTags = document.select("meta[name=description]");
                if (!metaTags.isEmpty()) {
                    scrapedData.add("Meta Description: " + metaTags.first().attr("content"));
                } else {
                    scrapedData.add("Meta Description: Not Found");
                }
            }

            if (paragraphsCheckbox.isSelected()) {
                scrapedData.add("Paragraphs:");
                Elements paragraphs = document.select("p");
                for (Element paragraph : paragraphs) {
                    scrapedData.add(paragraph.text());
                }
            }

            if (linksCheckbox.isSelected()) {
                scrapedData.add("Links:");
                Elements links = document.select("a[href]");
                for (Element link : links) {
                    scrapedData.add(link.attr("href"));
                }
            }

            if (imagesCheckbox.isSelected()) {
                scrapedData.add("Images:");
                Elements images = document.select("img[src]");
                for (Element image : images) {
                    scrapedData.add(image.attr("src")); // Store image URLs
                }
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return scrapedData;
    }

    private List<String> scrapeImageUrls(String url) {
        List<String> imageUrls = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements images = document.select("img[src]");
            for (Element image : images) {
                String imageUrl = image.attr("src");
                imageUrls.add(imageUrl); // Add image URLs to list
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return imageUrls;
    }

    private void updateResultArea(List<String> scrapedData) {
        resultArea.setText(String.join("\n", scrapedData));
    }

    private void saveToCSV(List<String> data) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile() + ".csv")) {
                for (String line : data) {
                    writer.write(line + "\n");
                }
                writer.close();
                JOptionPane.showMessageDialog(this, "Data saved to CSV successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error while saving CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WebScraperUI webScraperUI = new WebScraperUI();
            webScraperUI.setVisible(true);
        });
    }
}
