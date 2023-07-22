package test.java.com.csvPlusPlus.Lambdas;

import main.java.com.csvPlusPlus.Lambdas.OnS3UploadCreateAthenaTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

@ExtendWith(MockitoExtension.class)
public class OnS3UploadCreateAthenaTableTest {

    @Mock
    private S3AsyncClient s3AsyncClient;

    @Mock
    private AthenaClient athenaClient;
    private OnS3UploadCreateAthenaTable lambda;

    @BeforeEach
    public void setup() {
        lambda = new OnS3UploadCreateAthenaTable(athenaClient, s3AsyncClient);
    }

    @Test
    public void sanitizeToAthenaCompliantName_WithSpecialChars_RemovesSpecialChars() {
        String sanitized = lambda.sanitizeToAthenaCompliantName("Test#Name!");
        assertEquals("TestName", sanitized, "Sanitized string should have removed special characters");
    }

    @Test
    public void sanitizeToAthenaCompliantName_WithWhiteSpaceDashesAndSpecialChars_ReplacesWithUnderscoresAndRemovesSpecialChars() {
        String sanitized = lambda.sanitizeToAthenaCompliantName("Test_N-a me! ! Wo#rd");
        assertEquals("Test_N_a_me__Word", sanitized, "Sanitized string should have replaced white spaces with underscores");
    }

    @Test
    void removeFileExtension_noExtension_returnsSameString() {
        String filename = "testFile";
        assertEquals("testFile", lambda.removeFileExtension(filename));
    }

    @Test
    void removeFileExtension_withExtension_removesExtension() {
        String filename = "test.File.txt";
        assertEquals("test.File", lambda.removeFileExtension(filename));
    }
    @Test
    void uniqueColumnNames_noDuplicates_returnsSameArray() {
        String[] columnNames = {"column1", "column2", "column3"};
        assertArrayEquals(new String[]{"column1", "column2", "column3"}, lambda.uniqueColumnNames(columnNames));
    }

    @Test
    void uniqueColumnNames_withDuplicates_returnsUniqueArray() {
        String[] columnNames = {"dd", "dd_1_", "dd_2_", "dd", "dd_1_", "dd_1__2_", "dd_1__1_", "dd", "dd_1_"};
        assertArrayEquals(new String[]{"dd",  "dd_1_",  "dd_2_",  "dd_3_",  "dd_1__1_",  "dd_1__2_",  "dd_1__1__1_",  "dd_4_",  "dd_1__3_"},
                lambda.uniqueColumnNames(columnNames));
    }

}