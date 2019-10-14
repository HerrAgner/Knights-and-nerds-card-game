package cards;

public class UnitCard extends Card {
    private int health;
    private int currentHealth;
    private int attack;
    private boolean fatigue;

    public UnitCard() {
        this("", 0, 0, 0);
    }

    public UnitCard(String name, int cost, int hp, int attack) {
        super(name, cost);
        this.health = hp;
        this.attack = attack;
        this.fatigue = false;
        this.currentHealth = hp;
    }

    public boolean setHp(int hp) {
        if (hp <= 10) {
            this.health = hp;
            return true;
        } else {
            return false;
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
        if(this.currentHealth > health){
            this.currentHealth = health;
        }
    }

    public boolean setAttack(int attack) {
        if (attack > 0 && attack <= 10) {
            this.attack = attack;
            return true;
        } else {
            return false;
        }
    }

    public int getHp() {
        return health;
    }

    public int getAttack() {
        return attack;
    }

    public boolean getFatigue() {
        return fatigue;
    }

    public void setFatigue(boolean fatigue) {
        this.fatigue = fatigue;
    }
}
