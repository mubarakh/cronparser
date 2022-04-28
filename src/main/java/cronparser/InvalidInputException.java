package cronparser;

public class InvalidInputException  extends Exception {

        private static final long serialVersionUID = 1234499076876456634L;

        public InvalidInputException(String s, int errorOffset) {
            super(s);
            this.errorOffset = errorOffset;
        }

        public int getErrorOffset () {
            return errorOffset;
        }


        private int errorOffset;
}

