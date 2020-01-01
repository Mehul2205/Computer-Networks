'''Python2 was used to create the game
'''
import socket
from tkinter import simpledialog, messagebox
import sys
import os
import turtle

HOST = 'localhost'
PORT = 9990

cwd = os.getcwd()
COORD_FILENAME = cwd+"\coord.txt"

turtle.speed(0)

coord_list=[]

coord = open(COORD_FILENAME);
for line in coord:
    line = line.split(',')
    nums = []
    for n in line:
        nums.append(int(n)) 
    coord_list.append(nums)

def gotoxy(x,y):
    '''Moves turltle to coordinates x,y without drawing
    '''
    turtle.penup()
    turtle.goto (x,y)
    turtle.pendown()
    
def draw_line(from_x, from_y, to_x, to_y):
    '''Draws line by coordinates
    '''
    gotoxy(from_x, from_y)
    turtle.goto(to_x,to_y)
    
def draw_circle(x, y, r):
    '''Draws circle from x,y with selected radius
    '''
    gotoxy(x,y)
    turtle.circle(r)
      
draw_steps =[draw_line,draw_line,draw_line,draw_line,draw_circle,draw_line,draw_line,draw_line,draw_line,draw_line]

def drawG(step):
    '''Draws one part
    '''
    turtle.color("black")
    #* - to unpack array
    draw_steps[step](*coord_list[step])
    
def deleteText(x,y):
    '''Draws white rectangle in x,y in order to delete text 
    '''
    turtle.begin_fill()
    turtle.color("white")
    gotoxy(x,y)
    for x in range(2):
        turtle.forward(500)
        turtle.left(90)
        turtle.forward(50)
        turtle.left(90)
    turtle.end_fill()

print("I am a client!")
print("Connect to server {}:{}".format(HOST,PORT))

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST,PORT))

answer = simpledialog.askstring('New game?', 'y/n')
if answer == 'n':
    sock.sendall(bytes("GOODBYE", 'utf-8'))
    sock.close()
    sys.exit()

sock.sendall(bytes("START", 'utf-8'))

data = sock.recv(1024).decode()
data = data.split(';')

if data[0]=="GUESS":
    number_of_letters = data[1]
    number_of_tries = data[2]
    
    gotoxy(-250,-150)
    turtle.color("blue")
    turtle.write("Welcome to game! Guess a word of {} letters".format(number_of_letters), font = ("Arial", 14, "normal"))
    
    while True:       
        answer = simpledialog.askstring("Enter a letter", "You have {} more tries".format(number_of_tries))
        sock.sendall(bytes("TRY;{}".format(answer), 'utf-8'))
        data = sock.recv(1024).decode()
        data = data.split(';')
        deleteText(-250,-150)
        
        if data[0]=="MISTAKE":
            gotoxy(-250,-150)
            turtle.color("red")
            turtle.write("Oops! You've already guessed that letter", font = ("Arial", 20, "normal"))
            
        elif data[0]=="TRUE":
            deleteText(-50,100)
            gotoxy(-50,100)
            turtle.color("black")
            turtle.write("Good guess: {}".format(data[1]), font = ("Arial", 20, "normal"))
            
        elif data[0]=="WIN":
            gotoxy(-150,200)
            turtle.color("green")
            turtle.write("You won!", font = ("Arial", 28, "normal"))
            deleteText(-50,100)
            gotoxy(-50,100)
            turtle.color("black")
            turtle.write("Good guess: {}".format(data[1]), font = ("Arial", 20, "normal"))
            break
                
        elif data[0]=="FALSE":
            gotoxy(-250,-150)
            turtle.color("red")
            number_of_tries = data[1]  
            turtle.write("Oops! That letter is not in my word" , font = ("Arial", 20, "normal"))
            drawG(9-int(number_of_tries))
        
        elif data[0]=="FAIL":
            drawG(9)
            gotoxy(-200,200)
            turtle.color("red")
            turtle.write("You lose. The word was " + data[1], font = ("Arial", 24, "normal"))
            break

sock.sendall(bytes("GOODBYE", 'utf-8'))
turtle.Screen().bye()
sock.close()