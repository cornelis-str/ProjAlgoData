import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TinySearchEngine implements TinySearchEngineBase {

    private ArrayList<DocumentNode> index = new ArrayList<DocumentNode>();
    private boolean unique;

    public void insert(Word word, Attributes attr){
        Node tmp = new Node(word, attr);

        //System.out.println("word: " + word);
        if(index.size() == 0){
            int n = fcharSearch(tmp.word.word.charAt(0), null);
            if(n == -1){
                return;
            }
            index.add(new DocumentNode(tmp.attr.document));
            index.get(0).firstChar[n] = new FirstChar(tmp.word.word.charAt(0));
            index.get(0).firstChar[n].nodeArray.add(tmp);
        } else {
            int m = docSearch(tmp.attr.document.name, index);
            if(unique){                                                                                         //m == -1 => doc fanns inte
                int n = fcharSearch(tmp.word.word.charAt(0), null);
                if(n == -1){
                    return;
                }
                index.add(m, new DocumentNode(tmp.attr.document));
                index.get(m).firstChar[n] = new FirstChar(tmp.word.word.charAt(0));
                index.get(m).firstChar[n].nodeArray.add(tmp);
            } else {
                int n = fcharSearch(tmp.word.word.charAt(0), index.get(m).firstChar);
                if(n == -1){
                    return;
                }
                if(unique){
                    index.get(m).firstChar[n] = new FirstChar(tmp.word.word.charAt(0));
                    index.get(m).firstChar[n].nodeArray.add(tmp);                                           //på vilken bokstav börjar den
                } else {
                    int o = NodeSearch(tmp.word.word, index.get(m).firstChar[n].nodeArray);
                    index.get(m).firstChar[n].nodeArray.add(o, tmp);                                        //på vilken plats ska denna läggas till på

                }
            }
        }
    }

    public List<Document> search (String query){
        if(query.equals("")){
            System.out.println("????????????????????");
            return null;
        }

        ArrayList<DocumentHolder> ret = parser(query);

        //converting to return:
        LinkedList<Document> documents = new LinkedList<Document>();
        for(DocumentHolder documentHolder : ret){
            documents.add(documentHolder.attr.document);
        }
        return documents;
    }

    private ArrayList<DocumentHolder> parser(String query){

        boolean sort = false;
        boolean sortbycount = false;
        boolean sortbypop = false;
        boolean sortbyocc = false;
        boolean ascending = false;

        ArrayList<DocumentHolder> ret = new ArrayList<DocumentHolder>();
        String[] sentence = query.split(" ");

        for(int i = 0; i < sentence.length; i++) {
            if (sentence[i].equals("orderby")) {
                sort = true;
                if (i + 1 > sentence.length) {
                    throw new UnsupportedOperationException("???");
                } else {
                    if (sentence[i + 1].equals("count")) {
                        sortbycount = true;
                    } else if (sentence[i + 1].equals("popularity")) {
                        sortbypop = true;
                    } else if (sentence[i + 1].equals("occurrence")) {
                        sortbyocc = true;
                    }
                    if (i + 2 < sentence.length) {
                        if (sentence[i + 2].equals("asc")) {
                            ascending = true;
                        }
                    }
                    //remove words
                    //search with same conditions as before with the new query that has been split and "orderby" and "descending" / "ascending" have been removed
                    String[] sentencequery = new String[i];
                    for (int j = 0; j < i; j++) {
                        sentencequery[j] = sentence[j];
                    }

                    if (sentencequery.length == 1) {
                        System.out.println(sentencequery[0]);
                        ret = simpleSearch(sentencequery[0]);
                    } else {
                        ret = unionSearch(sentencequery);
                    }
                }
            }
        }

        if(!sort){
            if(sentence.length == 1){
                ret = simpleSearch(sentence[0]);
            } else {
                ret = unionSearch(sentence);
            }
        } else {
            if(sortbyocc) {
                ret = Sort.Bubblesort(ret, 0);
            } else if(sortbycount){
                ret = Sort.Bubblesort(ret, 1);
            } else if(sortbypop){
                ret = Sort.Bubblesort(ret, 2);
            }
            if(ascending){
                Collections.reverse(ret);
            }
        }

        /*
        for(DocumentHolder docH : ret){
            System.out.println("Document: " + docH.attr.document.name + "\t\t Count: " +docH.count + "\t\t Occurrence: " + docH.attr.occurrence + "\t\t Popularity: " + docH.attr.document.popularity);
        }
        */

        return ret;
    }

    private ArrayList<ArrayList<DocumentHolder>> union = new ArrayList<ArrayList<DocumentHolder>>();

    private ArrayList<DocumentHolder> unionSearch(String[] words){
        union.clear();
        
        for(int i = 0; i < words.length; i++) {
            ArrayList<DocumentHolder> tmp = simpleSearch(words[i]);
            ArrayList<DocumentHolder> tmp2 = new ArrayList<DocumentHolder>();
            for(int j = 0; j < tmp.size(); j++){
                tmp2.add(tmp.get(j));
            }
            union.add(tmp2);
        }

        while(union.size() > 1){
            ArrayList<DocumentHolder> tmp3 = merge(union.get(0), union.get(1));
            union.remove(0);
            union.remove(0);
            union.add(tmp3);
        }
        return union.get(0);
    }

    private ArrayList<DocumentHolder> merge (ArrayList<DocumentHolder> a, ArrayList<DocumentHolder> b){
        ArrayList<DocumentHolder> ret = new ArrayList<DocumentHolder>();
        ret.addAll(a);

        for(int i = 0; i < b.size(); i++){
            boolean exists = false;
            for(int j = 0; j < ret.size(); j++){
                if(b.get(i).attr.document.name.equals(ret.get(j).attr.document.name)){
                    exists = true;
                    if(b.get(i).attr.occurrence < ret.get(j).attr.occurrence){
                        int tmp = b.get(i).count + ret.get(j).count;
                        ret.remove(j);
                        ret.add(j, new DocumentHolder(b.get(i).word, b.get(i).attr, tmp));
                    } else {
                        ret.get(j).count += b.get(i).count;
                    }
                }
            }
            if(!exists){
                ret.add(b.get(i));
            }
        }
        return ret;
    }

    private ArrayList<DocumentHolder> simple = new ArrayList<DocumentHolder>();

    private ArrayList<DocumentHolder> simpleSearch(String query){
        simple.clear();
        for(DocumentNode docNode : index){
            simpleSearchHelper(query, docNode);
        }
        return simple;
    }

    private void simpleSearchHelper(String query, DocumentNode docNode) {
        ArrayList<Node> tmp = new ArrayList<Node>();

        int m = fcharSearch(query.charAt(0), null);
        if( m == -1) {
            return;
        }
        int n = NodeSearch(query, docNode.firstChar[m].nodeArray);

        if (!unique) {                                                                //add all occurences of the same word to a new linked list
            tmp.clear();
            tmp.add(docNode.firstChar[m].nodeArray.get(n));
            int i = 1;
            outerloop:
            if (docNode.firstChar[m].nodeArray.size() > n + 1) {
                while (docNode.firstChar[m].nodeArray.get(n).word.word.equals(docNode.firstChar[m].nodeArray.get(n + i).word.word)) {
                    tmp.add(docNode.firstChar[m].nodeArray.get(n + i++));
                    if (n + i >= docNode.firstChar[m].nodeArray.size()) {
                        break outerloop;
                    }
                }
            }

            i = 1;
            outerloop2:
            if (n - 1 != -1) {
                while (docNode.firstChar[m].nodeArray.get(n).word.word.equals(docNode.firstChar[m].nodeArray.get(n - i).word.word)) {
                    tmp.add(docNode.firstChar[m].nodeArray.get(n - i++));
                    if (n - i == -1) {
                        break outerloop2;
                    }
                }
            }
            int count = 0;
            for (Node node : tmp) {
                count++;
            }
            Node tmp2 = tmp.get(0);
            for (Node node : tmp) {
                if (tmp2.attr.occurrence > node.attr.occurrence) {
                    tmp2 = node;
                }
            }
            simple.add(new DocumentHolder(tmp2.word, tmp2.attr, count));
        }
    }


    //to search of the correct position in the docarray
    private int docSearch(String key, ArrayList<DocumentNode> documentNodes) {
        return docSearch(key.toLowerCase(), documentNodes, 0, documentNodes.size());
    }
    private int docSearch(String key, ArrayList<DocumentNode> documentNodes, int lo, int hi) {
        if (hi <= lo) {
            unique = true;
            return lo;                                                                         //return hi and false if the word doesn't exist.
        }
        int mid = lo + (hi - lo) / 2;
        int cmp = documentNodes.get(mid).doc.name.toLowerCase().compareTo(key);
        if      (cmp > 0) return docSearch(key, documentNodes, lo, mid);
        else if (cmp < 0) return docSearch(key, documentNodes, mid+1, hi);
        else {
            unique = false;
            return mid;
        }
    }

    //to search for the correct position in the documents char array
    private int fcharSearch(char key, FirstChar[] firstChars) {
        int ret = -1;
        key = Character.toLowerCase(key);
        if(key >= 'a' && key <= 'z') {
            ret = key - 'a';
        } else if(key == '\''){
            ret = 27;
        }
        if(ret == -1){
            return -1;
        }
        if(firstChars != null) {
            if (firstChars[ret] == null) {
                unique = true;
            } else {
                unique = false;
            }
        }
        return ret;
    }

    //to search for the correct position in the NodeArray
    private int NodeSearch(String key, ArrayList<Node> nodeArray) {
        return NodeSearch(key.toLowerCase(), nodeArray, 0, nodeArray.size());
    }
    private int NodeSearch(String key, ArrayList<Node> nodeArray, int lo, int hi) {
        if (hi <= lo) {
            unique = true;
            return lo;                                                                         //return hi and true if the word doesn't exist.
        }
        int mid = lo + (hi - lo) / 2;
        int cmp = nodeArray.get(mid).compareTo(key);
        if      (cmp > 0) return NodeSearch(key, nodeArray, lo, mid);
        else if (cmp < 0) return NodeSearch(key, nodeArray, mid+1, hi);
        else {
            unique = false;
            return mid;
        }
    }

    public class DocumentHolder{
        Word word;
        Attributes attr;
        int count;

        public DocumentHolder(Word word, Attributes attr, int count){
            this.word = word;
            this.attr = attr;
            this.count = count;
        }
    }

    private class DocumentNode implements Comparable{
        Document doc;
        FirstChar[] firstChar = new FirstChar[28];

        private DocumentNode(Document doc){
            this.doc = doc;
        }

        public int compareTo(Object o) {
            return 0;
        }
    }

    private class FirstChar implements Comparable{
        char c;
        ArrayList<Node> nodeArray = new ArrayList<Node>();

        private FirstChar(char c){
            this.c = c;
        }

        public int compareTo(Object o) {
            return 0;
        }
    }

    private class Node implements Comparable{
        Word word;
        Attributes attr;

        private Node(Word word, Attributes attr){
            this.word = word;
            this.attr = attr;
        }

        public int compareTo(Object cmp) {
            return 0;
        }

        private int compareTo(String cmp){
            return word.word.toLowerCase().compareTo(cmp);
        }
    }

}