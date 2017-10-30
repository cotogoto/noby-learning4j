package jp.livlog.noby.tokenization.tokenizer;


import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;

import com.atilika.kuromoji.ipadic.Token;


public class KuromojiIpadicTokenizer implements Tokenizer {

  private List<Token> tokens;
  private int index;
  private TokenPreProcess preProcess;

  public KuromojiIpadicTokenizer (String toTokenize) {
    com.atilika.kuromoji.ipadic.Tokenizer tokenizer = new com.atilika.kuromoji.ipadic.Tokenizer();
    tokens = tokenizer.tokenize(toTokenize);
    index = (tokens.isEmpty()) ? -1:0;
  }


  @Override
  public int countTokens() {
    return tokens.size();
  }

  @Override
  public List<String> getTokens() {
    List<String> ret = new ArrayList<String>();
    while (hasMoreTokens()) {
      ret.add(nextToken());
    }
    return ret;
  }

  @Override
  public boolean hasMoreTokens() {
    if (index < 0)
      return false;
    else
      return index < tokens.size();
  }

  @Override
  public String nextToken() {
    if (index < 0)
      return null;

    Token tok = tokens.get(index);
    index++;
    if (preProcess != null)
      return preProcess.preProcess(tok.getSurface());
    else
      return tok.getSurface();
  }

  @Override
  public void setTokenPreProcessor(TokenPreProcess preProcess) {
    this.preProcess = preProcess;
  }

}
