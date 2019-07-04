package be.ugent.equatic.web.util;

public class Menu {

    private MenuItem selectedMenuItem;

    public Menu() {
    }

    public Menu(MenuItem selectedMenuItem) {
        this.selectedMenuItem = selectedMenuItem;
    }

    public boolean isActive(String menuItem) {
        return selectedMenuItem != null && selectedMenuItem.name().equals(menuItem);
    }
}
