# GuessTheMovie
Bollywood/Hollywood

Game Description:
This is a 2 player TurnBased Game. It's based on a very popular pape game played by school students in India. Similar to Hangman for movies.

Game Steps:
Player 1
1) Choose a player to challenge
2) Choose a movie from Bollywood/Hollywood. Pick your choice from the dropdown/enter directly and validate the movie. You will see some movie info like title, description and release date. If this is the movie you had chosen, click on Send.
3a) You will see two more options before you send the movie. Do you want the opponent to see vowels in the movie blanks. For Example: - If you send the movie Dil. Should the opponent see _ _ _ or  _ I _
3b) You can choose to write some text as hint for the opponent
4) Send the movie to your opponet

Player 2
1) The player chosen will receive a match request from google. He can choose to accept or decline the invitation.
2) If user accepts the invitation, he is redirected to a guessing movie page where he will blanks instead of movie name, hint if provided by Player 1 and a BOLLYWOOD/HOLLYWOOD sign at top. Every wrong guess draws a line through the letters BOLLYWOOD. You have 9 chances overall to guess the movie
3) Depending on whether you win or lose you will be redirected to results page and your score will be updated

Scoring:
Scoring is based on Leaderboard by google play services. When you win a match +5 points will be added to your score. Your leaderboard score will only reflect your maximum score. You can see your position as compared to other players.

Cancellation:
If you press back button from movie guessing page, the match will be cancelled.

Current Bugs:
1) The api used for getting movie info is slightly inaccurate because of which sometimes hindi movies will show as HOLLYWOOD.
2) Sometimes movie poster wont be displayed
3) In some rare cases you will get Error Handling Strings
