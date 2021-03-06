package com.aliceplatform.poker.ranker;

import com.aliceplatform.poker.cards.Card;
import com.aliceplatform.poker.cards.Rank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Card ranker responsible for identification of combinations based on {@link Card.Suit}:
 * {@link Rank.HandRank#ROYAL_FLUSH},
 * {@link Rank.HandRank#STRAIGHT},
 * {@link Rank.HandRank#FLUSH}
 */
class CardSuitRanker implements Ranker {

    private Ranker nextRanker;

    public CardSuitRanker(Ranker nextRanker) {
        this.nextRanker = nextRanker;
    }

    @Override
    public Rank rank(List<Card> cards) {
        List<Map.Entry<Card.Suit, List<Card>>> cardGroups = cards
                .stream().collect(Collectors.groupingBy(Card::getSuit)).entrySet()
                .stream().sorted(Map.Entry.comparingByValue((o1, o2) -> o2.size() - o1.size()))
                .collect(Collectors.toList());
        List<Card> cardList = cardGroups.get(0).getValue();
        boolean ordered = true;
        if (cardList.size() >= 5) {
            Collections.sort(cardList, (card1, card2) -> card2.getCardRank().ordinal() - card1.getCardRank().ordinal());
            for (int i = 0; i < 4; i++) {
                if (cardList.get(i).getCardRank().ordinal() - cardList.get(i + 1).getCardRank().ordinal() != 1) {
                    ordered = false;
                    break;
                }
            }
            if (cardList.get(0).getCardRank() == Card.CardRank.ACE && ordered) {
                return new Rank(Rank.HandRank.ROYAL_FLUSH, Card.CardRank.ACE);
            } else if (ordered) {
                return new Rank(Rank.HandRank.STRAIGHT_FLUSH, cardList.get(0).getCardRank());
            } else {
                return new Rank(Rank.HandRank.FLUSH, cardList.get(0).getCardRank());
            }
        } else {
            return nextRanker.rank(cards);
        }
    }
}
