/**
 * @file SQLCorpusConnection.java
 * @copyright Hamburger Zentrum für Sprach Korpora
 */
package de.uni_hamburg.corpora.sru;

import java.util.ArrayList;
import java.util.List;


public class AdvancedSearchResultSet {

    private int maxLength;
    private int totalLength;
    private ArrayList<AdvancedSearchResult> records;

    public AdvancedSearchResultSet(int maxLength) {
         this.maxLength = maxLength;
         records = new ArrayList<AdvancedSearchResult>();
    }

    public static AdvancedSearchResultSet merge(AdvancedSearchResultSet lhs,
            AdvancedSearchResultSet rhs) {
        if ((rhs == null) && (lhs == null)) {
            return null;
        } else if (lhs == null) {
            return rhs;
        } else if (rhs == null) {
            return lhs;
        } else {
            AdvancedSearchResultSet merged = new AdvancedSearchResultSet(
                lhs.getMaxLength() + rhs.getMaxLength());
            merged.records = new ArrayList<AdvancedSearchResult>(lhs.records);
            merged.records.addAll(rhs.records);
            merged.setTotalLength(lhs.getTotalLength() + rhs.getTotalLength());
            return merged;
        }
    }

     public int getLength(){
         return records.size();
     }

     public int getMaxLength(){
         return maxLength;
     }

     public int getTotalLength(){
         return totalLength;
     }

     public void setTotalLength(int totalLength){
         this.totalLength = totalLength;
     }

     public AdvancedSearchResult getRecordAt(int pos){

         return records.get(pos);
     }


     public boolean addRecord(AdvancedSearchResult rec){
         return records.add(rec);
     }

}

