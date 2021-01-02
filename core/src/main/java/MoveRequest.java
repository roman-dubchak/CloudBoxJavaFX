public class MoveRequest extends AbstractMassage{
    private final String dir;

    public String getDir() {
        return dir;
    }

    public MoveRequest(String dir) {
        this.dir = dir;
    }
}
