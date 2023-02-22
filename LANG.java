package unescan;

public enum LANG {
	
	LBL_SAVE_STATUS_UNSAVED("! Nouvelles données détectées !\n! Ne pas oublier d'enregistrer !");
	
	private final String Language = "fr";
	private String fr;
	
	LANG(String p_fr){
		fr = p_fr;
	}
	
	public String get(){
		if(Language.equals("fr")) return this.fr;
		else return "";
	}
	

}
