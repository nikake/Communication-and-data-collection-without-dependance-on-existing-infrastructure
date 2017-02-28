package tests.app;

import org.junit.*;
import static org.junit.Assert.*;

import main.app.MachineID;

public class MachineIDTest {

    private MachineID  mID;

    @Before
    public void init() {
        mID  = new MachineID();
    }

    @Test
    public void initMachineID() {
        new MachineID();
    }
}
