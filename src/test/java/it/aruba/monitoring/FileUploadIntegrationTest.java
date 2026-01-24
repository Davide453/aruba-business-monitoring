package it.aruba.monitoring;

import it.aruba.monitoring.repository.ProcessingErrorRepository;
import it.aruba.monitoring.repository.ServiceRecordRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FileUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceRecordRepository serviceRecordRepository;

    @Autowired
    private ProcessingErrorRepository processingErrorRepository;
    @BeforeEach
    void cleanDb() {
        processingErrorRepository.deleteAll();
        serviceRecordRepository.deleteAll();
    }

    @Test
    void shouldUploadCsvAndPersistValidAndInvalidRows() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "csv",
                "test.csv",
                "multipart/form-data",
                new ClassPathResource("test.csv").getInputStream()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file))
                .andExpect(status().isAccepted());

        assertEquals(2, serviceRecordRepository.count());
        assertEquals(1, processingErrorRepository.count());
    }
}