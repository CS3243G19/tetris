package tetris.heuristic;

import static org.junit.Assert.*;
import org.junit.*;

import tetris.feature.*;

import java.util.ArrayList;

public class HeuristicTest {
  @Test
  public void testHeuristicSaveWeights() {
    assertTrue(true);
  }

  @Test
  public void testHeuristicLoadWeights() {
    Heuristic heuristic = new Heuristic("heuristics_test.txt");
    assertArrayEquals(heuristic.getWeights(), new double[]{-0.123,-0.456,-0.789}, 0);
    ArrayList<Feature> features = heuristic.getFeatures();
    assertEquals(features.size(), 3);
    assertEquals(features.get(0).getClass(), HoleFeature.class);
    assertEquals(features.get(1).getClass(), RowsClearedFeature.class);
    assertEquals(features.get(2).getClass(), TotalHeightFeature.class);
  }
}
