package kriskires.artolela;

/* Picture model */

class Picture {
    private String label_ru;
    private String label_en;
    private String label_it;
    private String image;

    public Picture(String label_en, String label_ru, String image) {
        this.label_en = label_en;
        this.label_ru = label_ru;
        this.image = image;
    }

    String getLabel_ru() {
        return label_ru;
    }

    public void setLabel_ru(String label_ru) {
        this.label_ru = label_ru;
    }

    String getLabel_en() {
        return label_en;
    }

    public void setLabel_en(String label_en) {
        this.label_en = label_en;
    }

    String getLabel_it() {
        return label_it;
    }

    public void setLabel_it(String label_it) {
        this.label_it = label_it;
    }

    String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}