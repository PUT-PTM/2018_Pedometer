#include "stm32f4xx.h"
#include "stm32f4_discovery.h"
#include "tm_stm32f4_lis302dl_lis3dsh.h"
#include <math.h>
#include <time.h>

 #define SAMPLESIZE 4


double X, Y, Z;

int running = 0;

int stepCounter = 0;

double xSample[SAMPLESIZE];
double ySample[SAMPLESIZE];
double zSample[SAMPLESIZE];

int sampleCounter = 0;

volatile double xResult = 0;
volatile double yResult = 0;
volatile double zResult = 0;

long timeDiff;

long timer = 0;
long timer_old, timer_new;

int PRECISION = 100;
int MIN_TIMEWIN = 15; //0.25 of second
int MAX_TIMEWIN = 50;

int TIME_WALK = 25; //max time for running

int last_step = 0;

char mode;

int standing = 1;

double allMin = 9999;
double allMax = 0;
double allTreshold = 0;
double allResult = 0;
double allOld = 0;
double allNew = 0;


//start running the pedometer
void start() {
 running = 1;

 //starting treshold
 allMin = X + Y + Z;
 allMax = X + Y + Z;
 allTreshold = (allMin + allMax) / 2;

}

//stop running the pedometer
void stop() {
 running = 0;
}


//uart interrupt - recives information if pedometer is running or is stopped
void USART3_IRQHandler(void) {
 if (USART_GetITStatus(USART3, USART_IT_RXNE) != RESET) {
  if (USART3 -> DR == 'R') {
   GPIO_SetBits(GPIOD, GPIO_Pin_12); //turn on green led
   GPIO_ResetBits(GPIOD, GPIO_Pin_14); //turn off red led
   start();

  }
  //S - STOP
  if (USART3 -> DR == 'S') {
   GPIO_SetBits(GPIOD, GPIO_Pin_14); //turn on red led
   GPIO_ResetBits(GPIOD, GPIO_Pin_12); //turn off green led
   stop();
  }
 }
}

//send a character trough UART
void sendData(char character) {
 while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET);

 USART_SendData(USART3, character);

 while (USART_GetFlagStatus(USART3, USART_FLAG_TC) == RESET);
}

//check if the user is walking or running
void checkMode() {
 if (timeDiff < TIME_WALK) {
  mode = 'r'; //running
 } else {
  mode = 'w'; //walking
 }
}

//check if the user has taken any stapes in given time
void checkIfStanding() {
 if (timer - timer_old > 100 && standing == 0) {
  sendData('s');
  standing = 1;
 }
}

//send data to application about calculated step
void step() {
 timer_old = timer_new;
 timer_new = timer;
 timeDiff = timer_new - timer_old; //time in msec between last two steps
 if (timeDiff >= MIN_TIMEWIN && timeDiff <= MAX_TIMEWIN) {
  checkMode(); //checks if the user is walking or running
  sendData(mode);
  stepCounter++;
  standing = 0;
 }

}

void initUart() {
 RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE); //uart
 RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE); //rxd txd

 // konfiguracja linii Rx i Tx
 GPIO_InitTypeDef GPIO_InitStructure;
 GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10 | GPIO_Pin_11;
 GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
 GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
 GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
 GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
 GPIO_Init(GPIOC, & GPIO_InitStructure);

 GPIO_PinAFConfig(GPIOC, GPIO_PinSource10, GPIO_AF_USART3);
 GPIO_PinAFConfig(GPIOC, GPIO_PinSource11, GPIO_AF_USART3);
 USART_InitTypeDef USART_InitStructure;
 USART_InitStructure.USART_BaudRate = 9600;
 USART_InitStructure.USART_WordLength = USART_WordLength_8b;
 USART_InitStructure.USART_StopBits = USART_StopBits_1;
 USART_InitStructure.USART_Parity = USART_Parity_No;
 USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
 USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
 USART_Init(USART3, & USART_InitStructure);


 USART_Cmd(USART3, ENABLE);
}

void initUartIrq() {
 //nvic configuration structure
 NVIC_InitTypeDef NVIC_InitStructure2;
 // turning on the interrupt for reciving data
 USART_ITConfig(USART3, USART_IT_RXNE, ENABLE);
 NVIC_InitStructure2.NVIC_IRQChannel = USART3_IRQn;
 NVIC_InitStructure2.NVIC_IRQChannelPreemptionPriority = 0;
 NVIC_InitStructure2.NVIC_IRQChannelSubPriority = 0;
 NVIC_InitStructure2.NVIC_IRQChannelCmd = ENABLE;
 NVIC_Init( & NVIC_InitStructure2);
 // turning on UART intterupt
 NVIC_EnableIRQ(USART3_IRQn);
}

void initLeds() {

 RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE); //led
 GPIO_InitTypeDef GPIO_InitStructure;
 GPIO_InitStructure.GPIO_Pin = GPIO_Pin_12 | GPIO_Pin_14;
 GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
 GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
 GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;
 GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
 GPIO_Init(GPIOD, & GPIO_InitStructure);

 GPIO_SetBits(GPIOD, GPIO_Pin_14); //turn on red led
}


void checkForStep() {

 if (allResult > allMax) allMax = zResult;
 if (allResult < allMin && allResult != 0) allMin = allResult;

 sampleCounter++;

 //set new treshold
 if (sampleCounter > 20) {
  sampleCounter = 0;
  allTreshold = (allMin + allMax) / 2;
  //clear previous mix and min
  allMin = 999999;
  allMax = 0;
 }
 //save old sample
 allOld = allNew;

 //check if new sample is greater than given precision
 if (fabs(allNew - allResult) > PRECISION) allNew = allResult;

 //check for step condition
 if (allOld > allTreshold && allNew < allTreshold) step();
}


void incrementTimer() {
 timer++;
}


int main(void) {

 RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM4, ENABLE);

 TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
 TIM_TimeBaseStructure.TIM_Period = 42000;
 TIM_TimeBaseStructure.TIM_Prescaler = 49;
 TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;
 TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;
 TIM_TimeBaseInit(TIM4, & TIM_TimeBaseStructure);

 TIM_Cmd(TIM4, ENABLE);

 initUart();
 initUartIrq();
 initLeds();

 TM_LIS302DL_LIS3DSH_Init(TM_LIS3DSH_Sensitivity_2G, TM_LIS3DSH_Filter_800Hz);
 TM_LIS302DL_LIS3DSH_t Axes;

 for (;;) {
  if (running == 1) {
   if (TIM_GetFlagStatus(TIM4, TIM_FLAG_Update)) {
    incrementTimer();
    checkIfStanding();
    xResult = 0;
    yResult = 0;
    zResult = 0;

    //get samples from X,Y,Z axis
    for (int i = 0; i < SAMPLESIZE; i++) {
     TM_LIS302DL_LIS3DSH_ReadAxes( & Axes);
     xResult += Axes.X;
     yResult += Axes.Y;
     zResult += Axes.Z;
    }

    //take average from every axis
    xResult /= SAMPLESIZE;
    yResult /= SAMPLESIZE;
    zResult /= SAMPLESIZE;

    //sum all the axis
    allResult = xResult + yResult + zResult;

    checkForStep();

    TIM_ClearFlag(TIM4, TIM_FLAG_Update);
   }
  }

 }

}
