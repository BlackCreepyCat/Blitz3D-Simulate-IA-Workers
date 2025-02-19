Graphics 1024,768,32,2
SetBuffer BackBuffer()




Global BaseX = GraphicsWidth()/2
Global BaseY = GraphicsHeight()*.75
Global Food = 0
SpeedUp = 5


Type Worker
	Field x#,y#
	Field Carrying
	Field Direction#
	Field NewDirection#
	Field FoodExchanged
	Field Caller
End Type

Type Food
	Field x#,y#
	Field Quantity
End Type

Function FoodNew()
	F.Food = New Food
	F\x = Rand(0,GraphicsWidth()-1)
	F\y = Rand(0,GraphicsHeight()-1)
	F\Quantity = Rand(1,100)
End Function

Function WorkerNew()
	W.Worker = New Worker
	W\x = BaseX
	W\y = BaseY
	W\Carrying = 0
	W\direction = Rand(0,359)
	W\NewDirection = w\direction
End Function


Function Draw()
	
	For F.Food = Each Food
		Color 0,255,0
		Rect f\x-10,f\y-5,21,11,True
		Color 0,0,0
		Text f\x,f\y,f\quantity,True,True
		
	Next
	
	For W.Worker = Each Worker
		Color 150,150,150
		If w\carrying Then Color 0,255,0
		If w\caller Then Color 0,255,255: Oval w\x-count,w\y-count,count*2+1,count*2+1,False
		Oval w\x-5,w\y-5,11,11,True
		Color 0,0,0
		Line w\x,w\y,w\x+(Sin(w\direction)*5),w\y-(Cos(w\direction)*5)
	Next
	
	Color 255,0,0
	Oval BaseX - 20, BaseY-20,41,41,False
	Text basex,basey, food,True,True
End Function

Function UpdateWorkers()
	For W.Worker = Each Worker
	
		; if you're carrying food you're at 50% speed
		slowdown# = 1
		If w\carrying Then slowdown# = .5
		If w\caller Then slowdown = 0
		ad# = AngleDifference(w\direction,w\newdirection)
		
		If ad>90 Then slowdown = 0
		
		w\Direction = WrapAngle(w\Direction + (ad/20))
		
		w\x = w\x + (Sin(w\direction) * slowdown)
		w\y = w\y - (Cos(w\direction) * slowdown)
		
		If w\x < 0 Then w\newdirection = Rand(45,135)
		If w\y < 0 Then w\newdirection = Rand(135,225)
		If w\x > GraphicsWidth() Then w\newdirection = Rand(225,315)
		If w\y > GraphicsHeight() Then w\newdirection = (Rand(-45,45) + 360) Mod 360
		
		
		If w\caller = False Then
		
			If w\foodexchanged > 0 Then w\foodexchanged = w\foodexchanged - 1
			
			; Find another worker with food who's further away from the base than you
			; and take the food from him to free him to search for more
			If w\carrying = False And w\foodexchanged = 0 Then
				For w1.worker = Each worker
					d = Dis(w\x,w\y,w1\x,w1\y) 
					If d < 100 And w1\carrying = True And dis(w\x,w\y,basex,basey) < dis(w1\x,w1\y,basex,basey) Then
						w\newdirection = (ATan2(w1\y-w\y,w1\x-w\x) + 90+360) Mod 360
					EndIf
					
					If d < 300 And w1\caller = True 
						w\newdirection = (ATan2(w1\y-w\y,w1\x-w\x) + 90+360) Mod 360
					EndIf
					
					If d < 15 And w1\carrying = True And w\carrying = False Then
						w\carrying = True
						w1\carrying = False
						w1\foodexchanged = 100
						w1\newdirection = Rand(0,359)
					EndIf
				Next
			EndIf
		
		
			; Only find food if you're not carrying food
			If w\carrying = False Then 
				; Find food
				For F.Food = Each Food
					d = Dis(w\x,w\y,f\x,f\y) 
					If d < 100 Then
						w\newdirection = (ATan2(f\y-w\y,f\x-w\x) + 90+360) Mod 360
					EndIf
					
					If d < 20 Then
					; check for callers
						found = False
						For w1.worker = Each worker
							If Dis(w\x,w\y,w1\x,w1\y)<50 And w1\caller = True Then found = True
						Next
						; if no callers become a caller
						If found = False Then w\caller = True
					EndIf
									
					If d < 10 Then
						ReduceFood(F.Food)
						w\carrying = True
					EndIf
		
				Next
			EndIf
			

			
			; If you've got food, head for base
			If w\carrying Then
				w\newdirection = (ATan2(BaseY-w\y,BaseX-w\x) + 90+360) Mod 360
			EndIf
			
	
			
			; If you're at base drop off the food and go searching again		
			If Dis(w\x,w\y,BaseX,BaseY) < 20 And w\carrying Then
				w\carrying = False
				w\newdirection = Rand(0,359)
				Food = Food + 1
				
			EndIf
		Else
		; You are a caller
			Found = False
			For F.Food = Each Food
				If Dis(w\x,w\y,f\x,f\y) < 50 Then found = True
			Next
			If found = False Then w\caller = False
		EndIf
			
			
		
		
	Next
End Function

Function ReduceFood(F.Food)
	If f\quantity > 0 Then f\quantity = f\quantity - 1
	If f\quantity = 0 Then Delete f
End Function

Function Dis(x1#,y1#,x2#,y2#)
	x=(x1-x2)
	y=(y1-y2)
	Return Sqr((x*x)+(y*y))
End Function

Function AngleDifference#(angle1#,angle2#) 
	Return ((angle2 - angle1) Mod 360 + 540) Mod 360 - 180 
End Function

Function WrapAngle#(value#)
	Return value+360 Mod 360
End Function

For n=1 To 100
	FoodNew()
Next
For n=1 To 100
	workernew()
Next

Global count

Repeat
	Cls
	For n=1 To Speedup
	updateworkers()
	Next
	Draw()
	count = (count + 10) Mod 100
	Flip
Until KeyHit(1)