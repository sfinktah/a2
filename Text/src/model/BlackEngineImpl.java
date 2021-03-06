package model;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import model.interfaces.PokerCard;
import view.interfaces.BlackEngineCallback;

import java.util.*;

public class BlackEngineImpl implements BlackEngine {
   // Using constant value for seed allows for repeatable tests
   static private int seed = 0; // x12345678;
   private final Map<String, Player> players = new HashMap<>();
   private final ArrayList<BlackEngineCallback> gameEngineCallbacks = new ArrayList<>();
   private Deck deck;

   /**
    * Causes the currently executing thread to sleep (temporarily cease
    * execution) for the specified number of milliseconds, subject to
    * the precision and accuracy of system timers and schedulers. The thread
    * does not lose ownership of any monitors.
    *
    * @param millis the length of time to sleep in milliseconds
    *
    * @throws IllegalArgumentException if the value of {@code millis} is negative
    */
   private static void wait(int millis) {
      try {
         Thread.sleep(millis);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   /**
    * <pre>Deal cards to the player as follows
    *
    * 1. deal a card to the player
    * 2. call {@link BlackEngineCallback#nextCard(Player, PokerCard, BlackEngine)}
    * 3. continue looping until the player busts (default value of BlackEngine.BUST_LEVEL=42)
    * 4. call {@link BlackEngineCallback#bustCard(Player, PokerCard, BlackEngine)}
    * 5. call {@link BlackEngineCallback#result(Player, int, BlackEngine)}
    *    with final result for player (the pre bust total)
    * 6. update the player with final result so it can be retrieved later
    *
    * @param player
    *            the current player who will have their result set at the end of the hand
    * @param delay
    *            the delay between cards being dealt (in milliseconds (ms))
    *
    * @throws IllegalArgumentException thrown when delay param is {@literal <} 0 or {@literal >} 1000
    * </pre>
    */
   @Override
   public void dealPerson(Player player, int delay) throws IllegalArgumentException {
      int score = getScore(player, delay);

      // call all registered callbacks with players final score
      for (BlackEngineCallback gameEngineCallback : gameEngineCallbacks) {
         gameEngineCallback.result(player, score, this);
      }

      // If this were put before BlackEngineCallback.result, then there would be no need
      // to pass `result` as an argument, it could be read as a property of `Player`.

      player.setResult(score);

   }

   /**
    * <pre>Helper function to deal cards to a player or dealer:
    *
    * 1. deal a card
    * 2. call {@link BlackEngineCallback#nextCard(Player, PokerCard, BlackEngine)}
    *      or {@link BlackEngineCallback#nextHouseCard(PokerCard, BlackEngine)} if player is null
    * 3. continue looping until the player busts (default value of BlackEngine.BUST_LEVEL=42)
    * 4. call {@link BlackEngineCallback#bustCard(Player, PokerCard, BlackEngine)}
    *      or {@link BlackEngineCallback#houseBustCard(PokerCard, BlackEngine)} if player is null
    * </pre>
    *
    * @param player player to deal to (or null for dealer)
    * @param delay  the delay between cards being dealt (in milliseconds (ms))
    */
   private int getScore(Player player, int delay) {
      if (Objects.isNull(deck)) {
         this.deck = newDeck();
      }

      int score = 0;
      int cardScore = 0;
      PokerCard card;
      boolean finished = false;
      while (!finished) {
         // The score isn't updated until the next iteration of the loop, that way
         // if we bust, the score stays at the last valid result.
         score += cardScore;

         card = deck.dealCard();

         cardScore = card.getScore();

         if (score + cardScore == BUST_LEVEL) {
            finished = true;
            score += cardScore;
         }
         else if (score + cardScore > BUST_LEVEL) {
            for (BlackEngineCallback gameEngineCallback : gameEngineCallbacks) {
               if (Objects.nonNull(player)) {
                  gameEngineCallback.bustCard(player, card, this);
               }
               else {
                  gameEngineCallback.houseBustCard(card, this);
               }
            }
            break;
         }

         for (BlackEngineCallback gameEngineCallback : gameEngineCallbacks) {
            if (Objects.nonNull(player)) {
               gameEngineCallback.nextCard(player, card, this);
            }
            else {
               gameEngineCallback.nextHouseCard(card, this);
            }
         }

         wait(delay);
      }

      // if score is anything other than exactly 42, then we busted.
      return score;
   }

   private Deck newDeck() {
      Deck deck = new Deck(seed);
      // after seed is set on first deck instantiation, seed is set to 0 causing `Deck(seed)` to ignore seed
      seed = 0;
      deck.shuffle();
      return deck;
   }

   /**
    * <pre>Same as dealPerson() other than the two notes below but deals for the house and calls the
    * house versions of the callback methods on BlackEngineCallback, no player parameter is required.
    *
    * IMPORTANT NOTE 1: At the end of the round but before calling calling {@link BlackEngineCallback#houseResult(int, BlackEngine)}
    * this method should iterate all players and call {@link BlackEngine#applyWinLoss(Player, int)}
    * to update each player's points
    *
    * IMPORTANT NOTE 2: After calling {@link BlackEngineCallback#houseResult(int, BlackEngine)}
    * this method should also call {@link Player#resetBet()} on each player in preparation for
    * the next round
    *
    * @param delay
    *            the delay between cards being dealt (in milliseconds (ms))
    *
    * @throws IllegalArgumentException thrown when delay param is {@literal <} 0
    *
    * @see BlackEngine#dealPerson(Player, int)
    * </pre>
    */
   @Override
   public void dealHouse(int delay) throws IllegalArgumentException {
      int score = getScore(null, delay);

      /*
       * IMPORTANT NOTE 1: At the end of the round but before calling calling {@link BlackEngineCallback#houseResult(int, BlackEngine)}
       * this method should iterate all players and call {@link BlackEngine#applyWinLoss(Player, int)}
       * to update each player's points
       */

      // Oh how painful life is without lambdas (we could iterate players.values(), but lets spice it up)
      for (Map.Entry<String, Player> entry : players.entrySet()) {
         Player player = entry.getValue();
         applyWinLoss(player, score);
      }

      for (BlackEngineCallback gameEngineCallback : gameEngineCallbacks) {
         gameEngineCallback.houseResult(score, this);
      }

      /*
       * IMPORTANT NOTE 2: After calling {@link BlackEngineCallback#houseResult(int, BlackEngine)}
       * this method should also call {@link Player#resetBet()} on each player in preparation for
       * the next round
       */

      // This better belongs in applyWinLoss (but must follow spec)
      for (Player player : players.values()) {
         player.resetBet();
      }
   }

   /**
    * <pre>
    * A player's bet is settled by this method
    * i.e. win or loss is applied to update betting points
    * based on a comparison of the player result and the provided houseResult
    *
    * NOTE: This method is usually called from {@link BlackEngine#dealHouse(int)}
    * as described above but is included in the public interface to facilitate testing
    *  @param player - the Player to apply win/loss to
    * @param houseResult - a DicePair containing the house result
    */
   @Override
   public void applyWinLoss(Player player, int houseResult) {
      // Note: in a real casino the chips are deducted before the round commences,
      // making this slightly easier to implement (no conditional execution)

      int playerResult = player.getResult();
      if (playerResult > houseResult) {
         player.setPoints(player.getPoints() + player.getBet());
      }
      else if (playerResult < houseResult) {
         player.setPoints(player.getPoints() - player.getBet());
      }
      // else draw - no chips change hands
   }

   /**
    * <b>NOTE:</b> id is unique and if another player with same id is added
    * it replaces the previous player
    *
    * @param player - to add to game
    */
   @Override
   public void addPerson(Player player) {
      players.put(player.getPersonId(), player);
   }

   /**
    * @param id - id of player to retrieve (null if not found)
    *
    * @return the Player or null if Player does not exist
    */
   @Override
   public Player getPerson(String id) {
      return players.get(id);
   }

   /**
    * @param player - to remove from game
    *
    * @return true if the player existed and was removed
    */
   @Override
   public boolean removePerson(Player player) {
      return Objects.nonNull(players.remove(player.getPersonId()));
   }

   /**
    * the implementation should forward the call to the player class so the bet is set per player
    *
    * @param player the player who is placing the bet
    * @param bet    the bet in points
    *
    * @return true if the player had sufficient points and the bet was valid and placed
    *
    * @throws NullPointerException if player cannot be found
    * @see Player#setBet(int)
    */
   @Override
   public boolean placeBet(Player player, int bet) {
      Player p = getPerson(player.getPersonId());
      Objects.requireNonNull(p);
      return p.setBet(bet);
   }

   /**
    * @param gameEngineCallback <pre> a client specific implementation of BlackEngineCallback used to perform display updates etc.
    *                                                     Callbacks should be called in the order they were added
    *                                                     <b>NOTE:</b> you will use a different implementation of the BlackEngineCallback
    *                                                           for the console (assignment 1) and GUI (assignment 2) versions
    *                                                     </pre>
    *
    * @see BlackEngineCallback
    */
   @Override
   public void addBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      this.gameEngineCallbacks.add(Objects.requireNonNull(gameEngineCallback));
   }

   /**
    * @param gameEngineCallback - instance to be removed if no longer needed
    *
    * @return true if the gameEngineCallback existed
    *
    * @see BlackEngineCallback
    */
   @Override
   public boolean removeBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      return this.gameEngineCallbacks.remove(gameEngineCallback);
   }

   /**
    * <pre>
    * @return an unmodifiable collection (or a shallow copy) of all Persons
    * Collection is SORTED in ascending order by player id</pre>
    *
    * @see Player
    */
   @Override
   public Collection<Player> getAllPersons() {
      // Persons should be sorted by playerId automatically, as ArrayList constructor will sort by `compareTo()`
      return new ArrayList<>(players.values());
   }

   /**
    * A debug method to return a "HALF" deck of cards containing 28 unique cards (8 through to Ace) in<br>
    * random/shuffled order (i.e. should return a new deck that is random WRT previous one)
    *
    * @return a Deque (specific type of Collection) of PokerCard
    *
    * @see PokerCard
    */
   @Override
   public Deque<PokerCard> getShuffledHalfDeck() {
      return newDeck().asLinkedList();
   }

}
