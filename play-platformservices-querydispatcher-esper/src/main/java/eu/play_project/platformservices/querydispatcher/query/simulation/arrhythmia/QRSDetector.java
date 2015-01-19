/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia;

/**
 * @author ningyuan 
 * 
 * Nov 14, 2014
 *
 */
public class QRSDetector {
	
	static int SAMPLE_RATE = 250;	/* Sample rate in Hz. */
	static double MS_PER_SAMPLE = ( (double) 1000/ (double) SAMPLE_RATE);
	static int MS10	= ((int) (10/ MS_PER_SAMPLE + 0.5));
	static int MS25	= ((int) (25/MS_PER_SAMPLE + 0.5));
	static int MS30	= ((int) (30/MS_PER_SAMPLE + 0.5));
	static int MS80	= ((int) (80/MS_PER_SAMPLE + 0.5));
	static int MS95	= ((int) (95/MS_PER_SAMPLE + 0.5));
	static int MS100 = ((int) (100/MS_PER_SAMPLE + 0.5));
	static int MS125 = ((int) (125/MS_PER_SAMPLE + 0.5));
	static int MS150 = ((int) (150/MS_PER_SAMPLE + 0.5));
	static int MS160 = ((int) (160/MS_PER_SAMPLE + 0.5));
	static int MS175 = ((int) (175/MS_PER_SAMPLE + 0.5));
	static int MS195 = ((int) (195/MS_PER_SAMPLE + 0.5));
	static int MS200 = ((int) (200/MS_PER_SAMPLE + 0.5));
	static int MS220 = ((int) (220/MS_PER_SAMPLE + 0.5));
	static int MS250 = ((int) (250/MS_PER_SAMPLE + 0.5));
	static int MS300 = ((int) (300/MS_PER_SAMPLE + 0.5));
	static int MS360 = ((int) (360/MS_PER_SAMPLE + 0.5));
	static int MS450 = ((int) (450/MS_PER_SAMPLE + 0.5));
	static int MS1000 = SAMPLE_RATE;
	static int MS1500 = ((int) (1500/MS_PER_SAMPLE));
	static int DERIV_LENGTH = MS10;
	static int LPBUFFER_LGTH = ((int) (2*MS25));
	static int HPBUFFER_LGTH = MS125;
	static int PRE_BLANK = MS195;
	static int MIN_PEAK_AMP = 7; // Prevents detections of peaks smaller than 150 uV.
	
	static int WINDOW_WIDTH = MS80;		// Moving window integration width.
	static int FILTER_DELAY = (int) (((double) DERIV_LENGTH/2) + ((double) LPBUFFER_LGTH/2 - 1) + (((double) HPBUFFER_LGTH-1)/2) + PRE_BLANK);  // filter delays plus 200 ms blanking delay
	static int DER_DELAY = WINDOW_WIDTH + FILTER_DELAY + MS100;
	
	public int QRSFilter(int datum, boolean init)
	{
		int fdatum ;
	
		if(init)
			{
			hpfilt( 0, true ) ;		// Initialize filters.
			lpfilt( 0, true ) ;
			mvwint( 0, true ) ;
			deriv1( 0, true );
			deriv2( 0, true ) ;
			}
	
		fdatum = lpfilt( datum, false ) ;		// Low pass filter data.
		fdatum = hpfilt( fdatum, false ) ;	// High pass filter data.
		fdatum = deriv2( fdatum, false ) ;	// Take the derivative.
		fdatum = Math.abs(fdatum);			// Take the absolute value.
		fdatum = mvwint( fdatum, false ) ;	// Average over an 80 ms window .
		
		return(fdatum) ;
	}
	
	
	/*
	 * lpfilt
	 */
	long lp_y1 = 0, lp_y2 = 0;
	int [] lp_data = new int[LPBUFFER_LGTH];
	int lp_ptr = 0;
	
	
	private int lpfilt( int datum ,boolean init)
	{
		
		long y0 ;
		int output, halfPtr ;
		if(init)
			{
			for(lp_ptr = 0; lp_ptr < LPBUFFER_LGTH; ++lp_ptr)
				lp_data[lp_ptr] = 0 ;
			lp_y1 = lp_y2 = 0 ;
			lp_ptr = 0 ;
			}
		halfPtr = lp_ptr-(LPBUFFER_LGTH/2) ;	// Use halfPtr to index
		if(halfPtr < 0)							// to x[n-6].
			halfPtr += LPBUFFER_LGTH ;
		y0 = (lp_y1 * 2) - lp_y2 + datum - (lp_data[halfPtr] * 2) + lp_data[lp_ptr] ;
		lp_y2 = lp_y1;
		lp_y1 = y0;
		output = (int)(y0 / ((LPBUFFER_LGTH*LPBUFFER_LGTH)/4));
		lp_data[lp_ptr] = datum ;			// Stick most recent sample into
		if(++lp_ptr == LPBUFFER_LGTH)	// the circular buffer and update
			lp_ptr = 0 ;					// the buffer pointer.
		return(output) ;
	}
	
	/*
	 * hpfilt
	 */
	long hp_y = 0;
	int [] hp_data = new int[HPBUFFER_LGTH];
	int hp_ptr = 0;
	
	private int hpfilt( int datum, boolean init )
	{
		
		
		int z, halfPtr ;
	
		if(init)
			{
			for(hp_ptr = 0; hp_ptr < HPBUFFER_LGTH; ++hp_ptr)
				hp_data[hp_ptr] = 0 ;
			hp_ptr = 0 ;
			hp_y = 0 ;
			}
	
		hp_y += datum - hp_data[hp_ptr];
		halfPtr = hp_ptr-(HPBUFFER_LGTH/2) ;
		if(halfPtr < 0)
			halfPtr += HPBUFFER_LGTH ;
		z = (int)(hp_data[halfPtr] - (hp_y / HPBUFFER_LGTH));
	
		hp_data[hp_ptr] = datum ;
		if(++hp_ptr == HPBUFFER_LGTH)
			hp_ptr = 0 ;
	
		return( z );
	}
	
	/*
	 * deriv
	 */
	int [] derBuff = new int [DERIV_LENGTH];
	int derI = 0;
	
	private int deriv1(int x, boolean init)
	{
		
		int y ;
	
		if(init)
			{
			for(derI = 0; derI < DERIV_LENGTH; ++derI)
				derBuff[derI] = 0 ;
			derI = 0 ;
			return(0) ;
			}
	
		y = x - derBuff[derI] ;
		derBuff[derI] = x ;
		if(++derI == DERIV_LENGTH)
			derI = 0 ;
		return(y) ;
	}
	
	private int deriv2(int x, boolean init)
	{
		
		int y ;
	
		if(init)
			{
			for(derI = 0; derI < DERIV_LENGTH; ++derI)
				derBuff[derI] = 0 ;
			derI = 0 ;
			return(0) ;
			}
	
		y = x - derBuff[derI] ;
		derBuff[derI] = x ;
		if(++derI == DERIV_LENGTH)
			derI = 0 ;
		return(y) ;
	}
	
	
	/*
	 * mvwint
	 */
	long sum = 0;
	int mvw_data[] = new int[WINDOW_WIDTH];
	int mvw_ptr = 0;
	
	private int mvwint(int datum, boolean init)
	{
		
		int output;
		if(init)
			{
			for(mvw_ptr = 0; mvw_ptr < WINDOW_WIDTH ; ++mvw_ptr)
				mvw_data[mvw_ptr] = 0 ;
			sum = 0 ;
			mvw_ptr = 0 ;
			}
		sum += datum ;
		sum -= mvw_data[mvw_ptr] ;
		mvw_data[mvw_ptr] = datum ;
		if(++mvw_ptr == WINDOW_WIDTH)
			mvw_ptr = 0 ;
		if((sum / WINDOW_WIDTH) > 32000)
			output = 32000 ;
		else
			output = (int)(sum / WINDOW_WIDTH) ;
		return(output) ;
	}
	
	
	
	
	/**********************************
	 * 
	 * 
	 **********************************/
	double TH = .3125 ;

	int [] DDBuffer = new int[DER_DELAY];
	int DDPtr ;	/* Buffer holding derivative data. */
	int Dly  = 0 ;
	
	
	
	int det_thresh, qpkcnt = 0 ;
	int [] qrsbuf = new int[8]; 
	int [] noise = new int[8];
	int [] rrbuf = new int[8] ;
	int [] rsetBuff = new int[8];
	int rsetCount = 0 ;
	int nmean, qmean, rrmean ;
	int count, sbpeak = 0, sbloc, sbcount = MS1500 ;
	int maxder, lastmax ;
	int initBlank, initMax ;
	int preBlankCnt, tempPeak ;
	
	public int QRSDet( int datum, boolean init )
	{
		int fdatum, QrsDelay = 0 ;
		int i, newPeak, aPeak ;
		
		if( init )
		{
		for(i = 0; i < 8; ++i)
			{
			noise[i] = 0 ;	/* Initialize noise buffer */
			rrbuf[i] = MS1000 ;/* and R-to-R interval buffer. */
			}

			qpkcnt = maxder = lastmax = count = sbpeak = 0 ;
			initBlank = initMax = preBlankCnt = DDPtr = 0 ;
			sbcount = MS1500 ;
			QRSFilter(0,true) ;	/* initialize filters. */
			Peak(0,true) ;
		}

		fdatum = QRSFilter(datum, false); /* Filter data. */
		
		/* Wait until normal detector is ready before calling early detections. */

		aPeak = Peak(fdatum, false) ;
		if(aPeak < MIN_PEAK_AMP)
			aPeak = 0 ;

		// Hold any peak that is detected for 200 ms
		// in case a bigger one comes along.  There
		// can only be one QRS complex in any 200 ms window.

		newPeak = 0 ;
		if(aPeak > 0 && preBlankCnt == 0)			// If there has been no peak for 200 ms
			{										// save this one and start counting.
			tempPeak = aPeak ;
			preBlankCnt = PRE_BLANK ;			// MS200
			}

		else if(aPeak == 0 && preBlankCnt > 0)	// If we have held onto a peak for
			{										// 200 ms pass it on for evaluation.
			if(--preBlankCnt == 0)
				newPeak = tempPeak ;
			}

		else if(aPeak > 0)							// If we were holding a peak, but
			{										// this ones bigger, save it and
			if(aPeak > tempPeak)				// start counting to 200 ms again.
				{
				tempPeak = aPeak ;
				preBlankCnt = PRE_BLANK ; // MS200
				}
			else if(--preBlankCnt == 0)
				newPeak = tempPeak ;
			}
		
		/* Save derivative of raw signal for T-wave and baseline shift discrimination. */
		
		DDBuffer[DDPtr] = deriv1( datum, false) ;
		if(++DDPtr == DER_DELAY)
			DDPtr = 0 ;
		
		
		
		/* Initialize the qrs peak buffer with the first eight 	*/
		/* local maximum peaks detected.						*/

		if( qpkcnt < 8 ){
			++count ;
			if(newPeak > 0) 
				count = WINDOW_WIDTH ;
			if(++initBlank == MS1000)
				{
				initBlank = 0 ;
				qrsbuf[qpkcnt] = initMax ;
				initMax = 0 ;
				++qpkcnt ;
				if(qpkcnt == 8)
					{
					qmean = mean( qrsbuf, 8 ) ;
					nmean = 0 ;
					rrmean = MS1000 ;
					sbcount = MS1500+MS150 ;
					det_thresh = thresh(qmean,nmean) ;
					}
				}
			if( newPeak > initMax )
				initMax = newPeak ;
			}

		else	/* Else test for a qrs. */
			{
			++count ;
			if(newPeak > 0){
				
				
				/* Check for maximum derivative and matching minima and maxima
				   for T-wave and baseline shift rejection.  Only consider this
				   peak if it doesn't seem to be a base line shift. */
				   
				if(BLSCheck(DDBuffer, DDPtr) == 0){


					// Classify the beat as a QRS complex
					// if the peak is larger than the detection threshold.

					if(newPeak > det_thresh){
						moveArray(qrsbuf);
						//memmove(&qrsbuf[1], qrsbuf, MEMMOVELEN) ;
						qrsbuf[0] = newPeak ;
						qmean = mean(qrsbuf,8) ;
						det_thresh = thresh(qmean,nmean) ;
						moveArray(rrbuf);
						//memmove(&rrbuf[1], rrbuf, MEMMOVELEN) ;
						rrbuf[0] = count - WINDOW_WIDTH ;
						rrmean = mean(rrbuf,8) ;
						sbcount = rrmean + (rrmean >> 1) + WINDOW_WIDTH ;
						count = WINDOW_WIDTH ;

						sbpeak = 0 ;

						lastmax = maxder ;
						maxder = 0 ;
						QrsDelay =  WINDOW_WIDTH + FILTER_DELAY ;
						initBlank = initMax = rsetCount = 0 ;
					}

					// If a peak isn't a QRS update noise buffer and estimate.
					// Store the peak for possible search back.


					else{
						moveArray(noise);
						//memmove(&noise[1],noise,MEMMOVELEN) ;
						noise[0] = newPeak ;
						nmean = mean(noise,8) ;
						det_thresh = thresh(qmean,nmean) ;

						// Don't include early peaks (which might be T-waves)
						// in the search back process.  A T-wave can mask
						// a small following QRS.

						if((newPeak > sbpeak) && ((count-WINDOW_WIDTH) >= MS360)){
							sbpeak = newPeak ;
							sbloc = count  - WINDOW_WIDTH ;
						}
					}
				}
			}
			
			/* Test for search back condition.  If a QRS is found in  */
			/* search back update the QRS buffer and det_thresh.      */

			if((count > sbcount) && (sbpeak > (det_thresh >> 1))){
				moveArray(qrsbuf);
				//memmove(&qrsbuf[1],qrsbuf,MEMMOVELEN) ;
				qrsbuf[0] = sbpeak ;
				qmean = mean(qrsbuf,8) ;
				det_thresh = thresh(qmean,nmean) ;
				moveArray(rrbuf);
				//memmove(&rrbuf[1],rrbuf,MEMMOVELEN) ;
				rrbuf[0] = sbloc ;
				rrmean = mean(rrbuf,8) ;
				sbcount = rrmean + (rrmean >> 1) + WINDOW_WIDTH ;
				QrsDelay = count = count - sbloc ;
				QrsDelay += FILTER_DELAY ;
				sbpeak = 0 ;
				lastmax = maxder ;
				maxder = 0 ;

				initBlank = initMax = rsetCount = 0 ;
			}
		}
		
		// In the background estimate threshold to replace adaptive threshold
		// if eight seconds elapses without a QRS detection.

		if( qpkcnt == 8 )
			{
			if(++initBlank == MS1000)
				{
				initBlank = 0 ;
				rsetBuff[rsetCount] = initMax ;
				initMax = 0 ;
				++rsetCount ;

				// Reset threshold if it has been 8 seconds without
				// a detection.

				if(rsetCount == 8)
					{
					for(i = 0; i < 8; ++i)
						{
						qrsbuf[i] = rsetBuff[i] ;
						noise[i] = 0 ;
						}
					qmean = mean( rsetBuff, 8 ) ;
					nmean = 0 ;
					rrmean = MS1000 ;
					sbcount = MS1500+MS150 ;
					det_thresh = thresh(qmean,nmean) ;
					initBlank = initMax = rsetCount = 0 ;
					}
				}
			if( newPeak > initMax )
				initMax = newPeak ;
			}
		
		
		return(QrsDelay) ;
	}
	
	
	/**************************************************************
	* peak() takes a datum as input and returns a peak height
	* when the signal returns to half its peak height, or 
	**************************************************************/
	int peak_max , peak_timeSinceMax, peak_lastDatum;
	
	private int Peak( int datum, boolean init ){
		
		int pk = 0 ;

		if(init)
			peak_max = peak_timeSinceMax = 0 ;
			
		if(peak_timeSinceMax > 0)
			++peak_timeSinceMax ;

		if((datum > peak_lastDatum) && (datum > peak_max))
			{
			peak_max = datum ;
			if(peak_max > 2)
				peak_timeSinceMax = 1 ;
			}

		else if(datum < (peak_max >> 1))
			{
			pk = peak_max ;
			peak_max = 0 ;
			peak_timeSinceMax = 0 ;
			Dly = 0 ;
			}

		else if(peak_timeSinceMax > MS95)
			{
			pk = peak_max ;
			peak_max = 0 ;
			peak_timeSinceMax = 0 ;
			Dly = 3 ;
			}
		peak_lastDatum = datum ;
		return(pk) ;
	}
	
	
	/********************************************************************
	mean returns the mean of an array of integers.  It uses a slow
	sort algorithm, but these arrays are small, so it hardly matters.
	********************************************************************/

	private int mean(int [] array, int datnum){
		long sum ;
		int i ;

		for(i = 0, sum = 0; i < datnum; ++i)
			sum += array[i] ;
		sum /= datnum ;
		return (int)sum ;
	}
	
	/****************************************************************************
	 thresh() calculates the detection threshold from the qrs mean and noise
	 mean estimates.
	****************************************************************************/

	private int thresh(int qmean, int nmean){
		int thrsh, dmed ;
		double temp ;
		dmed = qmean - nmean ;
	/*	thrsh = nmean + (dmed>>2) + (dmed>>3) + (dmed>>4); */
		temp = dmed ;
		temp *= TH ;
		dmed = (int) temp ;
		thrsh = nmean + dmed ; /* dmed * THRESHOLD */
		return(thrsh) ;
	}
	
	
	/***********************************************************************
	BLSCheck() reviews data to see if a baseline shift has occurred.
	This is done by looking for both positive and negative slopes of
	roughly the same magnitude in a 220 ms window.
    ***********************************************************************/

	private int BLSCheck(int [] dBuf,int dbPtr){
		int max, min, maxt=0, mint=0, t, x ;
		max = min = 0 ;
	
		for(t = 0; t < MS220; ++t)
			{
			x = dBuf[dbPtr] ;
			if(x > max)
				{
				maxt = t ;
				max = x ;
				}
			else if(x < min)
				{
				mint = t ;
				min = x;
				}
			if(++dbPtr == DER_DELAY)
				dbPtr = 0 ;
			}
	
		maxder = max ;
		min = -min ;
		
		/* Possible beat if a maximum and minimum pair are found
			where the interval between them is less than 150 ms. */
		   
		if((max > (min>>3)) && (min > (max>>3)) && (Math.abs(maxt - mint) < MS150))
			return (0) ;
		else
			return (1) ;
	}
	
	
	private void moveArray(int [] array){
		for(int i = array.length - 1; i > 0; i--){
			array[i] = array[i-1];
		}
	}
}
