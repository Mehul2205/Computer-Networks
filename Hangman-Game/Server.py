'''Python2 was used to create the game
'''
import random
import string
import os
import socketserver

cwd = os.getcwd()
WORDLIST_FILENAME = cwd +"\words.txt"

HOST = 'localhost'
PORT = 9990

def loadWords():
    """
    Returns a list of valid words. Words are strings of lowercase letters.    
    """
    print("Loading word list from file...")
    inFile = open(WORDLIST_FILENAME, 'r')
    line = inFile.readline()
    # wordlist: list of strings
    wordlist = line.split()
    print("  ", len(wordlist), "words loaded.")
    return wordlist
    
def chooseWord(wordlist):
    """
    wordlist (list): list of words (strings)
    Returns a word from wordlist at random
    """
    return random.choice(wordlist)
    
def isWordGuessed(secretWord, lettersGuessed):
    '''
    secretWord: string, the word the user is guessing
    lettersGuessed: list, what letters have been guessed so far
    returns: boolean, True if all the letters of secretWord are in lettersGuessed;
      False otherwise
    '''
    result = False
    for c in secretWord:
        if c in lettersGuessed:
            result = True
        else:
            return False
    return result

def getGuessedWord(secretWord, lettersGuessed):
    '''
    secretWord: string, the word the user is guessing
    lettersGuessed: list, what letters have been guessed so far
    returns: string, comprised of letters and underscores that represents
      what letters in secretWord have been guessed so far.
    '''
    resultString = ""
    for c in secretWord:
        if c in lettersGuessed:
            resultString = resultString+c
        else:
            resultString = resultString+'_'
    return resultString

def getAvailableLetters(lettersGuessed):
    '''
    lettersGuessed: list, what letters have been guessed so far
    returns: string, comprised of letters that represents what letters have not
    yet been guessed.
    '''
    availableLetters = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z']
    for c in lettersGuessed:
        availableLetters.remove(c)
    availableLettersString = ''
    for e in availableLetters:
        availableLettersString = availableLettersString + e
    return availableLettersString

class MHandler(socketserver.BaseRequestHandler):
    
    def handle(self):
        # Load the list of words into the variable wordlist
        wordlist = loadWords()
        secretWord = chooseWord(wordlist).lower()
        print(secretWord)
        
        lettersGuessed = []
        
        self.data = self.request.recv(1024).decode()
        print("Client {} tells: {}".format(self.client_address[0], self.data))
        
        if self.data == "START":
            tryCount = 10
            self.request.sendall(bytes("GUESS;{};{}".format(str(len(secretWord)),tryCount), 'utf-8'))
            while True:
                self.data = self.request.recv(1024).decode()
                data = self.data.split(';')
                if data[0]=="TRY":
                    print("Client {} tells: {}".format(self.client_address[0], self.data))
                    
                    guess = data[1]
                    
                    if guess in lettersGuessed:
                        self.request.sendall(bytes("MISTAKE", 'utf-8'))
                    elif guess in secretWord:
                        lettersGuessed.append(guess) 
                        guessedWord = getGuessedWord(secretWord, lettersGuessed);
                        
                        if isWordGuessed(secretWord, lettersGuessed):
                            self.request.sendall(bytes("WIN;{}".format(secretWord), 'utf-8'))
                            print("Client {} win!".format(self.client_address[0]))
                            break
                        else:    
                            self.request.sendall(bytes("TRUE;{}".format(guessedWord), 'utf-8'))
                    
                    else:
                        lettersGuessed.append(guess)    
                        tryCount -= 1
                        if tryCount ==0:
                            self.request.sendall(bytes("FAIL;{}".format(secretWord), 'utf-8'))
                            break
                        else:
                            self.request.sendall(bytes("FALSE;{}".format(tryCount), 'utf-8'))
                            
        elif self.data == 'GOODBYE':
            print("Client went away...")    
        
server = socketserver.TCPServer((HOST,PORT), MHandler)
print('Game Server is started!')

server.serve_forever()