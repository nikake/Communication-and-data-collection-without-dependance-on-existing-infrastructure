package tests.app;

import org.junit.*;
import static org.junit.Assert.*;

import main.app.MachineID;

public class MachineIDTest {

    private static final int ADDRESS_LENGTH = 6;
    private MachineID  mID;

    @Before
    public void init() {
        mID  = new MachineID();
    }

    @Test
    public void initMachineID() {
        new MachineID();
    }

    @Test
    public void machineIDShouldBe6BytesInLength() {
        byte[] macAddress = mID.getMachineID();
        assertEquals(ADDRESS_LENGTH, macAddress.length);
    }
}
