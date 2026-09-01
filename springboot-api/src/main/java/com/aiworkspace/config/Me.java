package com.aiworkspace.config;

public class Me {
    public static void main(String[] args) {
        String smile = "I am fine";
        String reality = "I am tired";

        // Local class lets you keep methods inside main()
        class Heart {
            boolean alive = true;
            String feeling;
            String tear;
            String memories;

            void hidePain() {
                feeling =null;
                tear = "private";
                memories = "never deleted";
            }

            void keepGoing() {}

            void say(String message) {
                System.out.println(message);
            }
        }

        Heart me = new Heart();

        while (me.alive) {
            me.keepGoing();
            me.hidePain();
            me.say("It's Okay");
            break; // Prevents "unreachable code" error
        }
        // Nobody sees this part
        // Still waiting for better days
        System.out.println(reality);
    }
}

/*
package com.aiworkspace.config;



public class me {

    public static void main(String[] args) {

        String smail="I am fine";

        String reality="I am tired";


        while(alive)

        {

            keepGoing();

            hidePain();

            say(" It's Okei")

        }

// Nobody sees this part


        private void hidePain(){



            felling=null;

            tear="private ";

            momories="never deleted";

        }

// Still waiting for better days

        System.out.println(reality);

    }

}*/
