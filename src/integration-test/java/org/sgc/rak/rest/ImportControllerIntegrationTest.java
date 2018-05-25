package org.sgc.rak.rest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sgc.rak.core.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = { Application.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ImportControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DataSource dataSource;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * Asserts that the number of compounds in the database is a specific value.
     *
     * @param count The expected value.
     * @throws SQLException If an error occurs.
     */
    private void assertCompoundCount(long count) throws SQLException {

        long actualCount;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("select count(1) from compound");
                ResultSet rs = stmt.executeQuery()) {
            rs.next();
            actualCount = rs.getLong(1);
        }

        Assert.assertEquals("Unexpected compound count after test completed", count, actualCount);
    }

    private void deleteCompound(String compoundName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("delete from compound where compound_nm = ?")) {
            stmt.setString(1, compoundName);
            Assert.assertEquals("Row not deleted", 1, stmt.executeUpdate());
        }
    }

    private static InputStream getCsv(String resource) {
        return ImportControllerIntegrationTest.class.getResourceAsStream(resource);
    }
    @Test
    public void testImportActivityProfiles_happyPath_allUnmodified_noOptionalParams() throws Exception {

        ResultActions actions = testImportActivityProfiles_impl("import-activity-profiles-header-all-unmodified.csv",
            null, null, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(1)));
        assertCompoundCount(3);
    }

    @Test
    public void testImportActivityProfiles_happyPath_allUnmodified_noHeaderNoCommit() throws Exception {

        ResultActions actions = testImportActivityProfiles_impl("import-activity-profiles-no-header-all-unmodified.csv",
            false, null, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(1)));
        assertCompoundCount(3);
    }

    @Test
    public void testImportActivityProfiles_happyPath_allUnmodified_withHeaderNoCommit() throws Exception {

        ResultActions actions = testImportActivityProfiles_impl("import-activity-profiles-header-all-unmodified.csv",
            true, null, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(1)));
        assertCompoundCount(3);
    }

    @Test
    public void testImportActivityProfiles_happyPath_allUnmodified_noHeaderWithCommit() throws Exception {

        ResultActions actions = testImportActivityProfiles_impl("import-activity-profiles-no-header-all-unmodified.csv",
            false, true, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(1)));
        assertCompoundCount(3); // Unmodified rows => same count
    }

    @Test
    public void testImportActivityProfiles_happyPath_allUnmodified_withHeaderWithCommit() throws Exception {

        ResultActions actions = testImportActivityProfiles_impl("import-activity-profiles-header-all-unmodified.csv",
            true, true, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(1)));
        assertCompoundCount(3); // Unmodified rows => same count
    }

    private ResultActions testImportActivityProfiles_impl(String csv, Boolean headerRow, Boolean commitParam,
                                                 boolean expectSuccess) throws Exception {

        MockMultipartFile file = new MockMultipartFile("file", getCsv(csv));
        boolean requestParams = false;

        String url = "/admin/api/activityProfiles";
        if (headerRow != null) {
            url += "?headerRow=" + headerRow;
            requestParams = true;
        }
        if (commitParam != null) {
            url += (requestParams ? "&" : "?") + "commit=" + commitParam;
        }

        ResultActions actions;
        try {
            actions = mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                .file(file)
                .with(new ImportControllerTest.PatchRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA)
            )
            .andDo(print());
        } catch (NestedServletException e) {
            throw (Exception)e.getCause(); // For tests that test failure paths, throw the underlying exception
        }

        if (expectSuccess) {
            actions.andExpect(status().isOk()
            ).andReturn();
        }

        return actions;
    }

    @Test
    public void testImportCompounds_happyPath_oneNewOneModified_noHeaderNoCommit() throws Exception {

        ResultActions actions = testImportCompounds_impl("import-compounds-no-header-one-new-one-modified.csv",
            false, false, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(2)));
        assertCompoundCount(3); // Nothing added
    }

    @Test
    public void testImportCompounds_happyPath_oneNewOneModified_withHeaderNoCommit() throws Exception {

        ResultActions actions = testImportCompounds_impl("import-compounds-header-one-new-one-modified.csv",
            true, false, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(2)));
        assertCompoundCount(3); // Nothing added
    }

    @Test
    public void testImportCompounds_happyPath_oneNewOneModified_noHeaderWithCommit() throws Exception {

        ResultActions actions = testImportCompounds_impl("import-compounds-no-header-one-new-one-modified.csv",
            false, true, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(2)));
        assertCompoundCount(4);
    }

    @Test
    public void testImportCompounds_happyPath_oneNewOneModified_withHeaderWithCommit() throws Exception {

        ResultActions actions = testImportCompounds_impl("import-compounds-header-one-new-one-modified.csv",
            true, true, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(2)));
        assertCompoundCount(4);
    }

    @Test
    public void testImportCompounds_happyPath_oneNewOneModified_noOptionalParams() throws Exception {

        ResultActions actions = testImportCompounds_impl("import-compounds-header-one-new-one-modified.csv",
            null, null, true);

        actions.andExpect(jsonPath("$.fieldStatuses", hasSize(2)));
        assertCompoundCount(4);
    }

    private ResultActions testImportCompounds_impl(String csv, Boolean headerRow, Boolean commitParam,
                                                   boolean expectSuccess) throws Exception {

        MockMultipartFile file = new MockMultipartFile("file", getCsv(csv));
        boolean requestParams = false;

        String url = "/admin/api/compounds";
        if (headerRow != null) {
            url += "?headerRow=" + headerRow;
            requestParams = true;
        }
        if (commitParam != null) {
            url += (requestParams ? "&" : "?") + "commit=" + commitParam;
        }

        ResultActions actions;
        try {
            actions = mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                .file(file)
                .with(new ImportControllerTest.PatchRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA)
            )
                .andDo(print());
        } catch (NestedServletException e) {
            throw (Exception)e.getCause(); // For tests that test failure paths, throw the underlying exception
        }

        if (expectSuccess) {
            actions.andExpect(status().isOk()
            ).andReturn();
        }

        return actions;
    }
}
