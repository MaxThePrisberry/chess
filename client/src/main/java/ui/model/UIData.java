package ui.model;

import ui.ServerFacade;

public record UIData(ServerFacade.UIType uiType, String output) {}
