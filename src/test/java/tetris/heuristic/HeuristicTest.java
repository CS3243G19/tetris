package tetris.heuristic;

import static org.junit.Assert.*;
import org.junit.*;

import tetris.feature.*;

import java.io.File;

import java.util.ArrayList;

public class HeuristicTest {
  @Test
  public void testGetFeature() {
    Feature rowsClearedFeature = new RowsClearedFeature();
    assertEquals(2, (int) Heuristic.getFeatureIndex(rowsClearedFeature));
  }
}
