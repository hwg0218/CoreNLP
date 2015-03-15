package edu.stanford.nlp.dcoref;

import java.util.*;

import edu.stanford.nlp.util.Generics;

public class ScorerMUC extends CorefScorer {

  public ScorerMUC() {
    super(ScoreType.MUC);
  }

  @Override
  protected void calculateRecall(Document doc) {
    int rDen = 0;
    int rNum = 0;

    Map<Integer, Mention> predictedMentions = doc.allPredictedMentions;
    for(CorefCluster g : doc.goldCorefClusters.values()){
      if(g.corefMentions.size()==0) {
        SieveCoreferenceSystem.logger.warning("NO MENTIONS for cluster " + g.getClusterID());
        continue;
      }
      rDen += g.corefMentions.size()-1;
      rNum += g.corefMentions.size();

      Set<Integer> partitions = Generics.newHashSet();
      for (Mention goldMention : g.corefMentions){
        if(!predictedMentions.containsKey(goldMention.mentionID)) {  // twinless goldmention
          rNum--;
        } else {
          partitions.add(predictedMentions.get(goldMention.mentionID).corefClusterID);
        }
      }
      rNum -= partitions.size();
    }
    if (rDen != doc.allGoldMentions.size()-doc.goldCorefClusters.values().size()) {
      System.err.println("rDen is " + rDen);
      System.err.println("doc.allGoldMentions.size() is " + doc.allGoldMentions.size());
      System.err.println("doc.goldCorefClusters.values().size() is " + doc.goldCorefClusters.values().size());
    }
    assert(rDen == (doc.allGoldMentions.size()-doc.goldCorefClusters.values().size()));

    recallNumSum += rNum;
    recallDenSum += rDen;
  }

  @Override
  protected void calculatePrecision(Document doc) {
    int pDen = 0;
    int pNum = 0;
    Map<Integer, Mention> goldMentions = doc.allGoldMentions;

    for(CorefCluster c : doc.corefClusters.values()){
      if(c.corefMentions.size()==0) continue;
      pDen += c.corefMentions.size()-1;
      pNum += c.corefMentions.size();
      Set<Integer> partitions = Generics.newHashSet();
      for (Mention predictedMention : c.corefMentions){
        if(!goldMentions.containsKey(predictedMention.mentionID)) {  // twinless goldmention
          pNum--;
        } else {
          partitions.add(goldMentions.get(predictedMention.mentionID).goldCorefClusterID);
        }
      }
      pNum -= partitions.size();
    }
    assert(pDen == (doc.allPredictedMentions.size()-doc.corefClusters.values().size()));

    precisionDenSum += pDen;
    precisionNumSum += pNum;
  }
}
