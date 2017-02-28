package java;

import org.junit.*;
import static org.junit.Assert.*;

import main.java.MachineID;

public class MachineIDTest {

    private static final int ADDRESS_LENGTH = 6;
    private MachineID  mID;

    @Before
    public void init() {
        mID  = MachineID.getInstance();
    }

    @Test
    public void initMachineID() {
        MachineID.getInstance();
    }

    @Test
    public void machineIDShouldBe6BytesInLength() {
        byte[] macAddress = mID.getMachineID();
        assertEquals(ADDRESS_LENGTH, macAddress.length);
    }

    @Test
    public void thereShouldOnlyBeOneInstanceOfMachineID() {
        MachineID other = MachineID.getInstance();
        assertEquals(true, mID == other);
    }
}
