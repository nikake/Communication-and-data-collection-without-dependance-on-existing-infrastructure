package tests.app;

import org.junit.*;
import static org.junit.Assert.*;

import main.app.Application;

public class ApplicationTest {

    private Application application;

    @Before
    public void init() {
        Application app = new Application();
    }

    @Test
    public void initApplication() {
        new Application();
    }

}
