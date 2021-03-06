public class InputDefaultComesLast
{
    void method(int i) {
        // switch with last default
        switch (i) {
        case 1: break;
        case 2: break;
        default: 
            // do something :)
        }

        // switch w/o default (not a problem)
        switch (i) {
        case 1: break;
        case 2: break;
        }

        // VIOLATION!!! default is not the last one.
        switch (i) {
        case 1:
            break;
        default:
            break;
        case 2:
            break;
        }
    }
}

@interface InputDefaultComesLastAnnotation
{
    int blag() default 1;
}