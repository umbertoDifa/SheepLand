package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Francesco
 */
public class MapTest {

    private Map map;

    public MapTest() {
    }

    @Before
    public void setUp() {
        map = new Map();
        map.setUp();
    }

    /**
     * Test of setUp method, of class Map.
     */
    @Test
    public void testSetUp() {
        System.out.println("setUp");
        assertTrue(map.getRegions()[1].getNeighbourNodes().contains(map.getStreets()[0]));
        assertTrue(map.getRegions()[7].getNeighbourNodes().contains(map.getStreets()[7]));
        assertTrue(map.getRegions()[10].getNeighbourNodes().contains(map.getStreets()[27]));
        assertTrue(map.getRegions()[18].getNeighbourNodes().contains(map.getStreets()[41]));
        assertTrue(map.getRegions()[14].getNeighbourNodes().contains(map.getStreets()[32]));
        assertTrue(map.getRegions()[8].getNeighbourNodes().contains(map.getStreets()[12]));

        assertTrue(map.getStreets()[0].getNeighbourNodes().contains(map.getRegions()[1]));
        assertTrue(map.getStreets()[7].getNeighbourNodes().contains(map.getStreets()[8]));
        assertFalse(map.getStreets()[34].getNeighbourNodes().contains(map.getStreets()[6]));

    }

}