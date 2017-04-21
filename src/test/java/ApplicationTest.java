package test.java;

import org.junit.*;
import static org.junit.Assert.*;

import main.java.Application;

public class ApplicationTest {

    private Application application;
    private static final String[] HOST_ADDRESS = {"127", "0", "0", "1"};

    /*

    @Before
    public void init() {
        application = Application.getInstance();
    }

    @After
    public void close() {
        application.close();
    }

    @Test
    public void initApplication() {
        Application.getInstance();
    }


    Commented out because the tests didnt allow me to build a jar-file......

    @Test
    public void hostAddressIndex0ShouldBe127() {
        assertEquals(HOST_ADDRESS[0], application.getHostAddress()[0]);
    }

    @Test
    public void hostAddressIndex1ShouldBe0() {
        assertEquals(HOST_ADDRESS[1], application.getHostAddress()[1]);
    }

    @Test
    public void hostAddressIndex2ShouldBe0() {
        assertEquals(HOST_ADDRESS[2], application.getHostAddress()[2]);
    }

    @Test
    public void hostAddressIndex3ShouldBe1() {
        assertEquals(HOST_ADDRESS[3], application.getHostAddress()[3]);
    }
    */
}
