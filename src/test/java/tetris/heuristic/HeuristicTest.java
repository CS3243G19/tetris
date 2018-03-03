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

  @Test
  public void testHeuristicLoadWeights() {
    File file = new File("heuristics_test.txt");
    Heuristic heuristic = new Heuristic(file);
    assertArrayEquals(heuristic.getWeights(), new double[]{-0.123,-0.456,-0.789}, 0);
    ArrayList<Feature> features = heuristic.getFeatures();
    assertEquals(features.size(), 3);
    assertEquals(features.get(0).getClass(), HoleFeature.class);
    assertEquals(features.get(1).getClass(), RowsClearedFeature.class);
    assertEquals(features.get(2).getClass(), TotalHeightFeature.class);
  }

  @Test
  public void testHeuristicSaveAndLoadWeights() {
    ArrayList<Feature> features = new ArrayList<Feature>();
    features.add(new RowsClearedFeature());
    features.add(new TotalHeightFeature());
    features.add(new HoleFeature());
    features.add(new UnevenFeature());

    Heuristic heuristic = new Heuristic(features);

    File file = new File("heuristics_generated.txt");

    heuristic.save(file);

    Heuristic heuristic2 = new Heuristic(file);

    ArrayList<Feature> features1 = heuristic.getFeatures();
    ArrayList<Feature> features2 = heuristic2.getFeatures();

    double[] weights1 = heuristic.getWeights();
    double[] weights2 = heuristic2.getWeights();

    for (int i = 0; i < features1.size(); i++) {
      assertEquals(features1.get(i), features2.get(i));
    }

    assertArrayEquals(weights1, weights2, 0);

    file.delete();
  }
}
