# Magic Tech - Data scientist assessment


## My initial thoughts

Given the problem is tracking the alternating lunge activity, my initial thought process was to get the landmarks required detecting the lunge activity. The lower body is mainly active when performing a lunge activity. Once the points are there, I could perform a logical operation to analyze these landmarks better and see if I could detect the lunge activity. Once that's detected, the next task was to track the progress of each lunge and increment the number of lunges after every lunge completion.

To break down this problem into smaller steps and describe it briefly:

1. Extract the required landmarks
    
    I could see that MediaPipe gives 33 landmarks. The lower body mainly moves during a lunge. Hence, I extracted the points leftHip, rightHip, leftKnee, rightKnee, leftAnkle and rightAnkle.

2. Assess the lunge activity

   I used a simple approach by checking if one leg is bent and the other is straight. A leg is considered "bent" if the knee is significantly closer to the ankle than to the hip, suggesting a bent position. The distance between landmarks is used to estimate the angles or leg positions.

3. Track the progress of lunge activity

    To track the progress of a lunge activity using a progress bar, we need to continuously monitor the leg's bending state and update the progress bar based on the degree of the lunge. The progress bar should:

    Increase when the leg starts bending.
    Reach 100% when the lunge reaches its deepest position (i.e., when the leg is at maximum bend).
    Decrease back to 0 when the person returns to the standing position, completing a full lunge.


## Potential Improvements:

1. Angle Calculation

   We could improve the accuracy by calculating the exact angles between the hip, knee, and ankle, rather than using simple distances.

2. Noise Handling

    We could apply filtering to smooth out noisy data from the landmarks.