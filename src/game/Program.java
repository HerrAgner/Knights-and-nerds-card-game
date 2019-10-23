package game;

import cards.Card;
import cards.EffectCard;
import cards.SpellCard;
import cards.UnitCard;
import enums.Response;
import utilities.Input;

import java.util.Collection;
import static utilities.Printer.print;

public class Program {
    private CLI cli;
    protected Game game;
    private boolean running;
    private Input input;
    private Player activePlayer;
    private Player defendingPlayer;
    private Collection<Card> cardsOnHand;
    private Collection<Card> cardsOnTable;
    private Collection<Card> enemyCardsOnTable;

    public Program() {
        input = new Input();
        cli = new CLI(this);
        cli.createPlayers();
    }

    void startGame(String playerOneName, String playerTwoName, int cardPileSize) {
        game = new Game(playerOneName, playerTwoName, cardPileSize);
        cli.setGame(game);
        gameLoop();
    }

    private void gameLoop() {
        while (running) {
            activePlayer = game.getCurrentPlayer();
            defendingPlayer = game.getDefendingPlayer();
            cardsOnHand = activePlayer.getCardsOnHand();
            cardsOnTable = activePlayer.getCardsOnTable();
            enemyCardsOnTable = defendingPlayer.getCardsOnTable();

            cli.setVariables();
            game.startTurn();

            System.out.println(activePlayer.getName() + "'s turn");

            print(cli.printBoardAndCardsOnHand());
            print(cli.printHpAndMana());
            print(cli.menu);

            boolean menu = true;
            while (menu) {
                menu = menuSwitch();
            }
        }
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    private boolean menuSwitch() {
        int userInput;
        boolean printAll = true;

        userInput = input.validatedInput(6);

        switch (userInput) {
            case 1:
                print(cli.printBoardAndCardsOnHand());
                break;
            case 2:
                cli.printHpAndMana();
                printAll = false;
                break;
            case 3:
                playCard();
                break;
            case 4:
                attackWithCard();
                break;
            case 5:
                print(cli.endTurn);
                game.finishTurn();
                return false;
            case 6:
                endGame();
                break;
            default:
                print(cli.menu);
                break;
        }
        if (!game.shouldGameContinue()) {
            running = false;
        }
        if (printAll) {
            cli.printBoardAndCardsOnHand();
        }
        print(cli.printHpAndMana());
        print(cli.menu);
        return true;
    }

    private void playCard() {
        int chosenCard;
        int chosenDefendingCard;

        print("Which card do you want to play?");
        print(cli.printCards(cardsOnHand));

        chosenCard = input.validateChosenCard(cardsOnHand.size());

        Card card = (Card) cardsOnHand.toArray()[(chosenCard - 1)];

        Response[] response = game.playCard(card.getId());

        if (response[0] == Response.OK) {
            UnitCard unitCard;
            switch (response[1]) {
                case SPELL_CARD:
                    SpellCard spellCard = (SpellCard) card;
                    if (spellCard.getType().equals("Healer")) {
                        print(cli.printCards(cardsOnTable));
                        if (!spellCard.isMany()) {
                            print("Which card do you want to heal? (0 to heal you)");
                        }
                        useSpell(spellCard, cardsOnHand);
                    } else if (spellCard.getType().equals("Attacker")) {
                        print(cli.printCards(enemyCardsOnTable));
                        if (!spellCard.isMany()) {
                            print("Which card do you want to attack? (0 to attack player)");
                        }
                        useSpell(spellCard, enemyCardsOnTable);
                    }
                    break;
                case EFFECT_CARD:
                    EffectCard effectCard = (EffectCard) card;
                    if (effectCard.getEffectValue() < 0) {
                        print(cli.printCards(enemyCardsOnTable));
                        print("Which card do you want to debuff?");

                        chosenDefendingCard = input.validateChosenCard(enemyCardsOnTable.size());
                        unitCard = (UnitCard) enemyCardsOnTable.toArray()[chosenDefendingCard - 1];
                        game.useEffectCard(effectCard, unitCard);
                        cli.printEffectCardInfo(effectCard, unitCard);


                    } else {
                        print(cli.printCards(cardsOnTable));
                        print("Which card do you want to buff?");
                        chosenDefendingCard = input.validateChosenCard(cardsOnTable.size());
                        unitCard = (UnitCard) cardsOnTable.toArray()[chosenDefendingCard - 1];
                        game.useEffectCard(effectCard, unitCard);
                        cli.printEffectCardInfo(effectCard, unitCard);
                    }
                    sleep(4000);
                    break;
                case UNIT_CARD:
                    print("Played card " + card.getName());
                    break;
                default:
                    // Crazy place! How did you get here?
                    break;
            }
        } else if (response[0] == Response.ERROR) {
            switch (response[1]) {
                case TABLE_FULL:
                    print("To many cards on the table. Max 7.");
                    break;
                case TABLE_EMPTY:
                    print("No cards on table");
                    break;
                case COST:
                    print("Not enough mana.");
                    break;
            }
        }
    }

    private void attackWithCard() {
        int chosenCard;
        int chosenDefendingCard;
        if (cardsOnTable.size() >= 1) {
            print("Choose card: ");
            print(cli.printCards(cardsOnTable));
            chosenCard = input.validateChosenCard(cardsOnTable.size());
            var attackingCard = (UnitCard) cardsOnTable.toArray()[chosenCard - 1];
            if (attackingCard.getFatigue()) {
                print("\nCard is fatigue, wait one turn to attack!\n");
                print(cli.menu);
            } else {
                print("Attack card or player (0 for player): ");
                print(cli.printCards(enemyCardsOnTable));
                chosenDefendingCard = input.validateActionOnPlayerOrCard(enemyCardsOnTable.size());

                if (chosenDefendingCard == 0) {
                    game.attackPlayer(attackingCard);
                    print(cli.attackPlayerInfo(attackingCard));
                    cli.hpBarAnimation(attackingCard);
                    sleep(3000);
                } else if (chosenDefendingCard <= enemyCardsOnTable.toArray().length) {
                    var defendingCard = (UnitCard) enemyCardsOnTable.toArray()[chosenDefendingCard - 1];
                    game.attackCard(attackingCard, defendingCard);
                    print(cli.printAttackInfo(attackingCard, defendingCard));
                    sleep(3000);
                }
            }
        } else {
            print(
                    "",
                    "No cards on table. Choose another option",
                    "",
                    cli.menu);
        }
    }

    private void useSpell(SpellCard spellCard, Collection<Card> cards) {
        if (spellCard.isMany()) {
            game.useSpellOnCard(spellCard);
            print(cli.printAoeSpellInfo(spellCard));
            sleep(3000);
            return;
        }
        int chosenDefendingCard = input.validateActionOnPlayerOrCard(cards.size());
        if (chosenDefendingCard == 0) {
            game.useSpellOnPlayer(spellCard);
            print(cli.printSpellOnPlayerInfo(spellCard));
            cli.hpBarAnimation(spellCard);
        } else {
            UnitCard unitCard = (UnitCard) cards.toArray()[chosenDefendingCard - 1];
            game.useSpellOnCard(spellCard, unitCard);
            print(cli.printSpellOnCardInfo(spellCard, unitCard));
        }
        sleep(3000);
    }

    private void endGame() {
        System.exit(0);
    }
    void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
