package tetris.genetic;

public class WeightScorePair implements Comparable<WeightScorePair> {

    Double[] weight;
    Double score;

    public WeightScorePair(Double[] weight, Double score) {
        this.weight = weight;
        this.score = score;
    }

    public Double[] getWeight() {
        return this.weight;
    }

    public Double getScore() {
        return this.score;
    }

    @Override
    public int compareTo (WeightScorePair o) {
        // We invert for decremental sorting
        return Double.compare(o.getScore(), this.getScore());
    }

}
