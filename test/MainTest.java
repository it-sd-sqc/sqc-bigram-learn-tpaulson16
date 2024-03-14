import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

import edu.cvtc.bigram.*;

@SuppressWarnings({"SpellCheckingInspection"})
class MainTest {
  @Test
  void createConnection() {
    assertDoesNotThrow(
        () -> {
          Connection db = Main.createConnection();
          assertNotNull(db);
          assertFalse(db.isClosed());
          db.close();
          assertTrue(db.isClosed());
        }, "Failed to create and close connection."
    );
  }

  @Test
  void reset() {
    Main.reset();
    assertFalse(Files.exists(Path.of(Main.DATABASE_PATH)));
  }

  @Test
  void mainArgs() {
    assertAll(
        () -> {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          System.setOut(new PrintStream(out));
          Main.main(new String[]{"--version"});
          String output = out.toString();
          assertTrue(output.startsWith("Version "));
        },
        () -> {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          System.setOut(new PrintStream(out));
          Main.main(new String[]{"--help"});
          String output = out.toString();
          assertTrue(output.startsWith("Add bigrams"));
        },
        () -> assertDoesNotThrow(() -> {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          System.setErr(new PrintStream(out));
          Main.main(new String[]{"--reset"});
          String output = out.toString();
          assertTrue(output.startsWith("Expected"));
        }),
        () -> assertDoesNotThrow(() -> Main.main(new String[]{"./sample-texts/non-existant-file.txt"})),
        () -> assertDoesNotThrow(() -> Main.main(new String[]{"./sample-texts/empty.txt"}))
    );
  }

  // TODO: Create your test(s) below. /////////////////////////////////////////
  @Test
  void testAddBigram_BigramInDatabase() throws SQLException {
      Connection db = Main.createConnection();
      try {
          int wordId1 = Main.getId(db, "word1");
          int wordId2 = Main.getId(db, "word2");
          // Add a bigram to the database
          Main.addBigram(db, wordId1, wordId2);
          // Query the database to check if the bigram was inserted
          PreparedStatement statement = db.prepareStatement("SELECT COUNT(*) AS c FROM bigrams WHERE words_id = ? AND next_words_id = ?");
          statement.setInt(1, wordId1);
          statement.setInt(2, wordId2);
          ResultSet resultSet = statement.executeQuery();
          // Check if the bigram was inserted successfully
          assertTrue(resultSet.next(), "Expected one bigram to be inserted.");
          assertEquals(1, resultSet.getInt("c"), "Expected one bigram to be inserted.");
      } finally {
          db.close();
      }
  }
}