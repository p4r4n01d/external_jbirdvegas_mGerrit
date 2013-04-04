package com.aokp.gerrit.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.aokp.gerrit.R;
import com.aokp.gerrit.objects.JSONCommit;
import com.fima.cardsui.objects.Card;

/**
 * Created with IntelliJ IDEA.
 * User: jbird
 * Date: 4/3/13
 * Time: 3:44 PM
 */
public class PatchSetPropertiesCard extends Card {
    private final JSONCommit mJSONCommit;

    public PatchSetPropertiesCard(JSONCommit commit) {
        this.mJSONCommit = commit;
    }
    @Override
    public View getCardContent(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.properties_card, null);
        ((TextView) rootView.findViewById(R.id.prop_card_subject)).setText(mJSONCommit.getSubject());
        ((TextView) rootView.findViewById(R.id.prop_card_owner)).setText(mJSONCommit.getOwner());
        ((TextView) rootView.findViewById(R.id.prop_card_author)).setText(mJSONCommit.getAuthor().getName());
        ((TextView) rootView.findViewById(R.id.prop_card_committer)).setText(mJSONCommit.getCommitter().getName());
        /*
        TODO evaluate adding Owner/Author/Committer's email *privacy concerns*
         */
        return null;
    }
}
