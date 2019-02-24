package jp.livlog.noby.tokenization.tokenizerfactory;

import java.io.InputStream;

import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import jp.livlog.noby.tokenization.tokenizer.KuromojiIpadicTokenizer;

public class KuromojiIpadicTokenizerFactory implements TokenizerFactory {

    private TokenPreProcess preProcess;


    @Override
    public Tokenizer create(final InputStream paramInputStream) {

        throw new UnsupportedOperationException();
    }


    @Override
    public Tokenizer create(final String toTokenize) {

        if (toTokenize == null) {
            throw new IllegalArgumentException("Unable to proceed; no sentence to tokenize");
        }

        final KuromojiIpadicTokenizer ret = new KuromojiIpadicTokenizer(toTokenize);
        ret.setTokenPreProcessor(this.preProcess);
        return ret;
    }


    @Override
    public TokenPreProcess getTokenPreProcessor() {

        return this.preProcess;
    }


    @Override
    public void setTokenPreProcessor(final TokenPreProcess preProcess) {

        this.preProcess = preProcess;
    }

}
