package test.java;

import org.junit.*;
import static org.junit.Assert.*;

import main.java.Application;

public class ApplicationTest {

    private Application application;
    private static final String[] HOST_ADDRESS = {"192", "168", "1", ""};

    @Before
    public void init() {
        application = new Application();
    }

    @Test
    public void initApplication() {
        new Application();
    }

    @Test
    public void hostAddressIndex0ShouldBe192() {
        assertEquals(HOST_ADDRESS[0], application.getHostAddress()[0]);
    }

    @Test
    public void hostAddressIndex1ShouldBe168() {
        assertEquals(HOST_ADDRESS[1], application.getHostAddress()[1]);
    }

    @Test
    public void hostAddressIndex2ShouldBe1() {
        assertEquals(HOST_ADDRESS[1], application.getHostAddress()[2]);
    }
}
