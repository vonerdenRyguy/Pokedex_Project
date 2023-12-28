package firstJDBC;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;
//import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PokedexMenu extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel pokemonImageLabel;
    private JLabel pokemonStatLabel;
    private DefaultTableModel tableModel;
    private JTable pokemonTable;
    private JTextField searchField;
    private JPanel[] pokemonContainers;

    public PokedexMenu() {
        super("Pokemon GUI");
        //super.setBackground(Color.red);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);        

        // Panel for the top section
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        topPanel.setBackground(Color.RED);
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        pokemonImageLabel = new JLabel();
        pokemonStatLabel = new JLabel();
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(pokemonImageLabel);
        topPanel.add(pokemonStatLabel);

        // Panel for the left section with 2 buttons
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        JButton partyMakerButton = new JButton("Party Maker");
        partyMakerButton.setForeground(Color.yellow);
        partyMakerButton.setBackground(Color.blue);

        JButton questionsButton = new JButton("Questions");
        questionsButton.setForeground(Color.blue);
        questionsButton.setBackground(Color.yellow);
        leftPanel.add(partyMakerButton);
        leftPanel.add(questionsButton);

        // Panel for the section with the scrollable table
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
      
        tableModel = new DefaultTableModel(new Object[]{"Image", "ID", "Name", "Type 1", "HP", "Attack", "Defense", "Sp. Atk", "Sp. Def", "Speed"}, 0);
        pokemonTable = new JTable(tableModel);
        int imageHeight = 90;
        pokemonTable.setRowHeight(imageHeight);
        
        TableColumn column = pokemonTable.getColumnModel().getColumn(2);
        column.setPreferredWidth(100);

                
        // scrollPane object for the scroll wheel
        JScrollPane scrollPane = new JScrollPane(pokemonTable);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        
        // this is necessary to render the image in the table
        pokemonTable.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        pokemonTable.setDefaultRenderer(Object.class, new Type1ColorRenderer());


        // Panel that organizes the 3 panels made above
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        // Adds the content panel to the frame
        add(contentPanel);

        // calls the displayPokemon method that adds the info to the table
        displayPokemon();

        // ActionListener for the search button
        searchButton.addActionListener(e -> searchPokemon(searchField.getText()));

        // DocumentListener for the search field to perform live search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                liveSearch(searchField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                liveSearch(searchField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                liveSearch(searchField);
            }
        });

        // A ListSelectionListener for the table
        pokemonTable.getSelectionModel().addListSelectionListener(e -> {
            //  this line makes it so these statements are
            // only triggered when the user's selection is completed
            if (!e.getValueIsAdjusting()) {
                int selectedRow = pokemonTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Fetch the selected image and set the icon
                    ImageIcon selectedImageIcon = (ImageIcon) pokemonTable.getValueAt(selectedRow, 0);
                    if (selectedImageIcon != null) {
                        pokemonImageLabel.setIcon(selectedImageIcon);

                        // Extract the selected Pokemon ID from the table
                        int selectedPokemonID = selectedRow + 1;
                        displayPokemonStats(selectedPokemonID);
                    }
                }
            }
        });
        
        // action Listeners for partyMaker and questionsButton buttons
        partyMakerButton.addActionListener(e -> handlePartyMaker());
        questionsButton.addActionListener(e -> handleQuestions());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void displayPokemon() {
        // my credentials
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        // Set to keep hold of unique Pokemon IDs
        Set<Integer> uniquePokemonIds = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // this query gets the pokemon data for the table in Pokemon GUI
            String sql = "SELECT DISTINCT s.id, s.sprite_url, p.Name, p.Type1, p.HP, p.Attack, p.Defense, p.Sp_atk, p.Sp_def, p.Speed\r\n"
                    + "FROM pokedex.spriteimages s\r\n"
                    + "JOIN pokedex.pokemon p ON s.id = p.id\r\n"
                    + "WHERE s.id <= 890;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");

                        // Check if the Pokemon ID is already added to the table
                        // this is to ensure no repeats
                        if (!uniquePokemonIds.contains(id)) {
                            String imageUrl = resultSet.getString("sprite_url");
                            String name = resultSet.getString("Name");
                            String type1 = resultSet.getString("Type1");
                            int hp = resultSet.getInt("HP");
                            int attack = resultSet.getInt("Attack");
                            int defense = resultSet.getInt("Defense");
                            int spAtk = resultSet.getInt("Sp_atk");
                            int spDef = resultSet.getInt("Sp_def");
                            int speed = resultSet.getInt("Speed");

                            // this is how image url's are converted to actual images
                            Image image = getImageFromUrl(imageUrl);
                            if (image != null) {
                                ImageIcon imageIcon = new ImageIcon(image);

                                // Adding Pokemon data to the tableModel
                                tableModel.addRow(new Object[]{imageIcon, id, name, type1, hp, attack, defense, spAtk, spDef, speed});
                                // Add the Pokemon ID to the set to prevent duplicates
                                uniquePokemonIds.add(id);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void displayPokemonStats(int PokemonID) {
        // my credentials
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL query to retrieve Pokemon details including image URL
            String sql = "SELECT DISTINCT s.id, p.Name, p.Type1, p.Type2, p.Total, p.Generation\r\n"
                    + "FROM pokedex.spriteimages s\r\n"
                    + "JOIN pokedex.pokemon p ON s.id = p.id\r\n"
                    + "WHERE s.id <= 890 AND s.id = ?;";
            // this statement ensures the correct pokemon statistics are displayed from the search button or clicked in table
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, PokemonID);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("Name");
                        //String type1 = resultSet.getString("Type1");
                        String type2 = resultSet.getString("Type2");
                        int total = resultSet.getInt("Total");
                        int gen = resultSet.getInt("Generation");
                        
                        if (type2 == null || type2.isEmpty()) {
                            type2 = "None";
                        }
  
                        String statsText = String.format("<html>|  ID: %d  |  Name: %s  |  Type2: %s  |  Total: %d  |  Generation: %d  |</html>",
                                id, name, type2, total, gen);
                        pokemonStatLabel.setText(statsText);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void searchPokemon(String searchTerm) {
        // Iterate through the rows in the table to find a match
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nameInTable = (String) tableModel.getValueAt(i, 2);

            if (nameInTable.equalsIgnoreCase(searchTerm)) {
                // Found a match and update the stat label
                int selectedPokemonID = i + 1;
                displayPokemonStats(selectedPokemonID);

                // Scroll down to the corresponding row in the table
                pokemonTable.getSelectionModel().setSelectionInterval(i, i);
                pokemonTable.scrollRectToVisible(pokemonTable.getCellRect(i, 0, true));

                // Exit the loop once a match is found
                return;
            }
        }
    }
    

    private void liveSearch(JTextField textField) {
        
        String searchTerm = textField.getText();
        // list of 10 search suggestions
        List<String> suggestions = getSearchSuggestions(searchTerm);

        // Display suggestions in a scrollable pop-up menu
        JPopupMenu popupMenu = new JPopupMenu();
        int maxVisibleItems = 10;

        // loop that adds suggestions to pop up menu
        for (String suggestion : suggestions) {
            JMenuItem menuItem = new JMenuItem(suggestion);
            menuItem.addActionListener(e -> textField.setText(suggestion));            
            menuItem.addActionListener(e -> {
                textField.setText(suggestion);
                // Requests focus on the search field for user to keep typing
                textField.requestFocusInWindow();
            });
            popupMenu.add(menuItem);

            // Limits the number of visible items in the menu
            if (popupMenu.getComponentCount() >= maxVisibleItems) {
                break;
            }
        }

        // Display the pop-up menu below the search field
        int x = 0;
        int y = textField.getHeight();
        popupMenu.show(textField, x, y);
        textField.requestFocusInWindow();
    }


    private List<String> getSearchSuggestions(String searchTerm) {
        // Retrieve and return search suggestions from the database
        List<String> suggestions = new ArrayList<>();

        // my credentials
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String sql = "SELECT pokemon_name FROM pokedex.pokedescription WHERE pokemon_name LIKE ? ";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, "%" + searchTerm + "%");

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        suggestions.add(resultSet.getString("pokemon_name"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    private Image getImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handlePartyMaker() {
        JFrame partyFrame = new JFrame("Party Maker");
        partyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        partyFrame.setSize(575, 315);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        topPanel.setBackground(Color.RED);
        JTextField addPokemonField = new JTextField(15);
        int[] numInParty = {1};
        JLabel partyInstructions = new JLabel("Choose a Pokemon for Slot " + numInParty[0] + ": ");
        partyFrame.add(partyInstructions);
        JButton addPokemon = new JButton("Add to Party");

        topPanel.add(partyInstructions);
        topPanel.add(addPokemonField);
        topPanel.add(addPokemon);

        // Add DocumentListener for the search field to perform live search
        addPokemonField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                liveSearch(addPokemonField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                liveSearch(addPokemonField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                liveSearch(addPokemonField);
            }
        });
        
        // Declare party as a local variable within handlePartyMaker
        List<Pokemon> party = new ArrayList<>();
        
        JPanel vsPanel = new JPanel();
        vsPanel.setBackground(new Color(172, 17, 17));
        JButton vsButton = new JButton("Learn more about your party HERE!!!");
        vsPanel.add(vsButton);
        vsButton.setEnabled(false);
        vsButton.setBackground(Color.RED);

        vsButton.addActionListener(e -> {
            extraInfo(party);
        });

        // Counter to keep track of the current slot
        int[] currentSlot = {1};

        // Add ActionListener for the Add Pokemon button
        addPokemon.addActionListener(e -> {
            // Check if 6 Pokemon have already been chosen
            if (party.size() >= 6) {
                JOptionPane.showMessageDialog(partyFrame, "You have already chosen 6 Pokémon.", "Party Full", JOptionPane.WARNING_MESSAGE);
                partyInstructions.setText("Party Complete!!!!!  ");
                return;
            }

            addPokemonToParty(addPokemonField.getText(), party, currentSlot);
            
            numInParty[0]++;;
            if (numInParty[0] > 6) {
                partyInstructions.setText("Party Complete!!!!!  ");
            } else {
                partyInstructions.setText("Choose a Pokemon for Slot " + numInParty[0] + ": ");
            }

            // Check if 6 Pokemon have been chosen to enable the "V.S." button
            if (party.size() == 6) {
                vsButton.setEnabled(true);
            }
        });

        JPanel partyPanel = new JPanel(new GridLayout(3, 2));

        // Create 6 containers for Pokemon information
        pokemonContainers = new JPanel[6];
        for (int i = 0; i < pokemonContainers.length; i++) {
            pokemonContainers[i] = createPokemonContainer();
            partyPanel.add(pokemonContainers[i]);
        }

        

        partyFrame.add(topPanel, BorderLayout.NORTH);
        partyFrame.add(partyPanel, BorderLayout.CENTER);
        partyFrame.add(vsPanel, BorderLayout.SOUTH);

        partyFrame.setLocationRelativeTo(null);
        partyFrame.setVisible(true);
    }

    private void addPokemonToParty(String pokemonName, List<Pokemon> party, int[] currentSlot) {
        // gets the Pokemon details from the database based on the provided name
        Pokemon chosenPokemon = getPokemonDetails(pokemonName);

        if (chosenPokemon != null) {
            // Add the chosen Pokemon to the party list
            party.add(chosenPokemon);
            
            addPokemonToDB(chosenPokemon);

            // Update the party panel to reflect the changes
            updatePartyPanel(party, currentSlot[0]++);
        } else {
            // case where the Pokemon is not found
            System.out.println("Pokemon not found: " + pokemonName);
        }
    }
    
    private void addPokemonToDB(Pokemon pokemon) {
        // my credentials
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL query to insert the selected Pokemon into the partyOf6 table
            // this stores data into table permanently
            String insertSql = "INSERT INTO partyOfSix (name, type1, hp) VALUES (?, ?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStatement.setString(1, pokemon.getName());
                insertStatement.setString(2, pokemon.getType1());
                insertStatement.setInt(3, pokemon.getHp());

                // Execute the SQL insert command
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private JPanel createPokemonContainer() {
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(FlowLayout.LEFT));    
        
        container.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel();
        JLabel typeLabel = new JLabel();
        JLabel hpLabel = new JLabel();
        JLabel imageLabel = new JLabel();

        container.add(nameLabel);
        container.add(typeLabel);
        container.add(hpLabel);
        container.add(imageLabel);

        return container;
    }


    private void updatePartyPanel(List<Pokemon> party, int currentSlot) {
        int containerIndex = currentSlot - 1;
        JPanel pokemonContainer = pokemonContainers[containerIndex];

        JLabel nameLabel = (JLabel) pokemonContainer.getComponent(0);
        JLabel typeLabel = (JLabel) pokemonContainer.getComponent(1);
        JLabel hpLabel = (JLabel) pokemonContainer.getComponent(2);
        JLabel imageLabel = (JLabel) pokemonContainer.getComponent(3);

        Pokemon chosenPokemon = party.get(party.size() - 1);

        nameLabel.setText(chosenPokemon.getName());
        typeLabel.setText("Type: " + chosenPokemon.getType1());
        hpLabel.setText("HP: " + chosenPokemon.getHp());

        // Set background color based on the slot index
        setLabelBackgroundColor(pokemonContainer, containerIndex);

        ImageIcon imageIcon = chosenPokemon.getImage();
        imageLabel.setIcon(imageIcon);
    }

    private void setLabelBackgroundColor(JPanel labelPanel, int containerIndex) {
        Color backgroundColor;
        // Sets background colors based on container index
        switch (containerIndex) {
            case 0:
            case 3:
            case 4:
                backgroundColor = new Color(0, 153, 76);
                break;
            case 1:
            case 2:
            case 5:
                backgroundColor = new Color(7, 230, 68);
                break;
            default:
                backgroundColor = Color.LIGHT_GRAY;
        }
        labelPanel.setBackground(backgroundColor);
    }

    
    private Pokemon getPokemonDetails(String pokemonName) {
        // my credentials
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL query to retrieve Pokemon details including image URL
            String sql = "SELECT DISTINCT s.id, s.sprite_url, p.Name, p.Type1, p.HP\r\n"
                    + "FROM pokedex.spriteimages s\r\n"
                    + "JOIN pokedex.pokemon p ON s.id = p.id\r\n"
                    + "WHERE s.id <= 890 AND p.Name = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, pokemonName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String imageUrl = resultSet.getString("sprite_url");
                        String name = resultSet.getString("Name");
                        String type1 = resultSet.getString("Type1");
                        int hp = resultSet.getInt("HP");

                        // gets the image using the URL and create an ImageIcon
                        Image image = getImageFromUrl(imageUrl);
                        if (image != null) {
                            ImageIcon imageIcon = new ImageIcon(image);

                            // Creates and returns the Pokemon object
                            return new Pokemon(name, imageIcon, type1, hp);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return null if Pokemon details are not found
        return null;
    }

    private JPanel createPokemonContainer2(Pokemon pokemon, String description, int pokeNum) {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.setBackground(Color.WHITE);

        // Add image to the panel
        JLabel imageLabel = new JLabel(new ImageIcon(pokemon.getImage().getImage()));
        container.add(imageLabel, BorderLayout.WEST);

        // Add name and description to the panel
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(new JLabel(pokemon.getName()));
        
        JTextArea descriptionTextArea = new JTextArea("Description: " + description);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setEditable(false);
        textPanel.add(descriptionTextArea);
        textPanel.setBackground(Color.LIGHT_GRAY);
        container.add(textPanel, BorderLayout.CENTER);
        
        Color backgroundColor;
        // Sets the background color based on container index
        switch (pokeNum) {
            case 1:
            case 4:
            case 5:
                backgroundColor = new Color(0, 153, 76);
                break;
            case 2:
            case 3:
            case 6:
                backgroundColor = new Color(7, 230, 68);
                break;
            default:
                backgroundColor = Color.LIGHT_GRAY;
        }
        textPanel.setBackground(backgroundColor);
        descriptionTextArea.setBackground(backgroundColor);
        
        pokeNum++;

        return container;
    }

    private void extraInfo(List<Pokemon> userParty) {
        JFrame extraInfo = new JFrame("Extra Info");
        extraInfo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        extraInfo.setSize(600, 400);

        JPanel partyRatingPanel = new JPanel(new GridLayout(3, 2)); // 2 rows, 3 columns

        int pokeNum = 1;

        // Assuming Pokemon class has appropriate fields and methods
        for (Pokemon pokemon : userParty) {
            // Fetch additional information from the database
            String description = getPokemonDescriptionFromDB(pokemon.getName());

            // Create a panel for each Pokemon
            JPanel pokemonPanel = createPokemonContainer2(pokemon, description, pokeNum);

            // Add the Pokemon panel to the Party Rating panel
            partyRatingPanel.add(pokemonPanel);

            pokeNum++;
        }

        JScrollPane scrollPane = new JScrollPane(partyRatingPanel);
        extraInfo.add(scrollPane);

        extraInfo.setLocationRelativeTo(null);
        extraInfo.setVisible(true);
    }



    private String getPokemonDescriptionFromDB(String pokemonName) {
        
        // my credentials
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL query to retrieve Pokemon description
            String sql = "SELECT DISTINCT d.pokemon_name, d.description "
                    + "FROM pokedex.pokedescription d "
                    + "JOIN pokedex.pokemon p ON d.pokemon_name = p.Name "
                    + "WHERE p.Name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Set the value for the parameter
                statement.setString(1, pokemonName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Return the description if found
                        return resultSet.getString("description");
                    } else {
                        // Return null if the description is not found
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving description";
        }
    }

    // methods that handle the trivia part!
    private void handleQuestions() {
        // Fetch a random description and associated Pokemon from the database
        PokemonQuestion pokemonQuestion = getRandomPokemonQuestion();

        // Create a new frame for the questions
        JFrame questionsFrame = new JFrame("Pokemon TRIVIA");
        questionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        questionsFrame.setSize(600, 300);
        questionsFrame.setLayout(new BorderLayout());

        // Display the question and input field
        JLabel questionLabel = new JLabel("Who's that Pokemon?");
        String descriptionText = "<html>" + pokemonQuestion.getDescription() + "</html>";
        JLabel descriptionLabel = new JLabel(descriptionText);
        JTextField guessField = new JTextField(20);
        JButton submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(172, 17, 17));
        submitButton.setForeground(Color.WHITE);
        JLabel resultLabel = new JLabel();
        resultLabel.setBorder(null);;
        JLabel imageLabel = new JLabel();

        JPanel questionPanel = new JPanel(new GridLayout(4, 1));
        questionPanel.setBackground(Color.RED);
        questionPanel.add(questionLabel);
        questionPanel.add(descriptionLabel);
        questionPanel.add(guessField);
        questionPanel.add(submitButton);
        
        JPanel resultPanel = new JPanel(new GridLayout(2, 1));
        resultPanel.add(resultLabel);
        resultPanel.add(imageLabel); 
        

        questionsFrame.add(questionPanel, BorderLayout.NORTH);
        questionsFrame.add(resultLabel, BorderLayout.CENTER);
        
        // Add DocumentListener for the search field to perform live search
        guessField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                liveSearch(guessField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                liveSearch(guessField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                liveSearch(guessField);
            }
        });
        

        // Add ActionListener for the submit button
        submitButton.addActionListener(e -> {
            // Check the users guess
            String userGuess = guessField.getText().trim();
            boolean isCorrect = userGuess.equalsIgnoreCase(pokemonQuestion.getPokemonName());
            pokemonQuestion.decrementAttempts();

            // Update the result label
            if (isCorrect) {
                resultLabel.setText("Correct! Well done!");
                
                ImageIcon correctPokemonImage = pokemonQuestion.getImageIcon();
                imageLabel.setIcon(correctPokemonImage);
                submitButton.setVisible(false);
                submitButton.setBackground(Color.WHITE);
                               
               JButton playAgainButton = new JButton("Play Again");
                playAgainButton.setBackground(Color.RED);


                JPanel buttonPanel = new JPanel();
                buttonPanel.add(playAgainButton);

                // Add ActionListener for the play again button
                playAgainButton.addActionListener(playAgainEvent -> {
                    // Close the question frame
                    questionsFrame.dispose();
                    
                    handleQuestions();
                });

                // Add the button panel to the questions frame
                questionsFrame.add(buttonPanel, BorderLayout.SOUTH);
            } else {
                if (pokemonQuestion.getAttempts() == 2) {
                    resultLabel.setText("Incorrect. 2 attempts remaining!");
                } else if (pokemonQuestion.getAttempts() == 1) {
                    resultLabel.setText("Incorrect. 1 attempts remaining!");
                } else if (pokemonQuestion.getAttempts() <= 0 || isCorrect) {
                    resultLabel.setText("Sorry, you've run out of attempts. The correct answer is: " + pokemonQuestion.getPokemonName());
                    ImageIcon correctPokemonImage = pokemonQuestion.getImageIcon();
                    imageLabel.setIcon(correctPokemonImage);

                    guessField.setEnabled(false);
                    submitButton.setEnabled(false);

                    // Add buttons for going back to the main panel or playing again
                    JButton playAgainButton = new JButton("Play Again");
                    playAgainButton.setBackground(Color.RED);
                    submitButton.setBackground(Color.WHITE);
                    submitButton.setVisible(false);


                    JPanel buttonPanel = new JPanel();
                    buttonPanel.setBackground(new Color(172, 17, 17));
                    buttonPanel.add(playAgainButton);

                    // Add ActionListener for the play again button
                    playAgainButton.addActionListener(playAgainEvent -> {
                        // Close the question frame
                        questionsFrame.dispose();
                        handleQuestions();
                    });

                    // Add the button panel to the questions frame
                    questionsFrame.add(buttonPanel, BorderLayout.SOUTH);
                }

            }
            
        });

        questionsFrame.setLocationRelativeTo(null);
        questionsFrame.setVisible(true);
    }

    private PokemonQuestion getRandomPokemonQuestion() {
        // my credentials
        
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        List<PokemonQuestion> pokemonQuestions = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL query to retrieve a random Pokemon question with image URL
            String sql = "SELECT d.id, d.description, d.pokemon_name, s.sprite_url " +
                         "FROM pokedex.pokedescription d " +
                         "JOIN pokedex.spriteimages s ON d.id = s.id " +
                         "WHERE d.id < 891 " +
                         "ORDER BY RAND() LIMIT 1";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String description = resultSet.getString("description");
                        String pokemonName = resultSet.getString("pokemon_name");
                        String imageUrl = resultSet.getString("sprite_url");
                        int attempts = 3; // Set the initial number of attempts

                        // Create a PokemonQuestion object with the retrieved information
                        PokemonQuestion pokemonQuestion = new PokemonQuestion(id, description, pokemonName, attempts, imageUrl);
                        pokemonQuestions.add(pokemonQuestion);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pokemonQuestions.isEmpty() ? null : pokemonQuestions.get(0);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PokedexMenu());
    }
}
