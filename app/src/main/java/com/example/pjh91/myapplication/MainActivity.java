package com.example.pjh91.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private final int GOOGLE_STT = 1000, MY_UI=1001;				//requestCode. 구글음성인식, 내가 만든 Activity
    private ArrayList<String> mResult;									//음성인식 결과 저장할 list
    private String mSelectedString;										//결과 list 중 사용자가 선택한 텍스트
    private TextView mResultTextView;									//최종 결과 출력하는 텍스트 뷰
    private ArrayList<String> sMeg;									//음성인식 결과 저장할 list
    //////////////////////////////////////음성인식 변수들

    /** Called when the activity is first created. */
    public Socket cSocket = null;
    private String server = "192.168.18.21";  // 서버 ip주소
    private int port = 4444;                           // 포트번호

    public PrintWriter streamOut = null;
    public BufferedReader streamIn = null;

    public chatThread cThread = null;
    public String nickName = "지훈";
    public TextView tv;
    public EditText nickText;
    public EditText msgText;
    public ScrollView sv;
    ////////////////////////////////////////네트워크 변수들


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mic).setOnClickListener(this);				//구글 음성인식 앱 이용.
        findViewById(R.id.connBtn).setOnClickListener(this);
        findViewById(R.id.closeBtn).setOnClickListener(this);
        findViewById(R.id.sendBtn).setOnClickListener(this);
        //findViewById(R.id.hide).setOnClickListener(this);				//내가 만든 activity 이용.
        sv = (ScrollView)findViewById(R.id.scrollView1);
        tv = (TextView)findViewById(R.id.text01);
        //nickText = (EditText)findViewById(R.id.connText);
        msgText = (EditText)findViewById(R.id.chatText);

        logger("채팅을 시작합니다.");

        mResultTextView = (TextView)findViewById(R.id.result);		//결과 출력 뷰

        //추가한 라인    fcm
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.mic:
            {
                logger("마이크사용.");
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);			//intent 생성
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());	//음성인식을 호출한 패키지
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");							//음성인식 언어 설정
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "말을 하세요.");						//사용자에게 보여 줄 글자

                startActivityForResult(i, GOOGLE_STT);		//구글 음성인식 실행
            }
            break;
            case R.id.connBtn: // 접속버튼
                if (cSocket == null) {
                    //nickName = nickText.getText().toString();
                    logger("접속중입니다...");
                    connect(server, port , nickName);
                    logger("접속완료...");
                }
                break;
            case R.id.closeBtn: // 나가기 버튼
                if (cSocket != null) {
                    sendMessage("# [" + nickName + "]님이 나가셨습니다.");
                }
                break;
	     /*case R.id

		          //nickName = nickText.getText().toString();
		          logger("접속중입니다...");
		         // connect(server, port , nickName);
		          logger("접속완료...");

	    	 break;*/
            case R.id.sendBtn: // 메세지 보내기 버튼
                if (cSocket != null) {
                    String msgString = msgText.getText().toString();
                    if (msgString != null && !"".equals(msgString)) {
                        sendMessage("[" + nickName + "] " + msgString);
                        //sendMessage(msgString);
                        msgText.setText("");
                    }
                } else {
                    logger("접속을 먼저 해주세요.");
                }
                break;
        }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) { //빽(취소)키가 눌렸을때 종료여부를 묻는 다이얼로그 띄움
        if((keyCode == KeyEvent.KEYCODE_BACK))
        {
            AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
            d.setTitle("종료여부");
            d.setMessage("정말 종료 하시겠습니꺄?");
            d.setIcon(R.mipmap.ic_launcher);
            d.setPositiveButton("예",new DialogInterface.OnClickListener()
            {
                @Override public void onClick(DialogInterface dialog, int which)
                { // TODO Auto-generated method stub
                    sendMessage("stop");
                    MainActivity.this.finish();
                }
            });

            d.setNegativeButton("아니요",new DialogInterface.OnClickListener()
            {
                @Override public void onClick(DialogInterface dialog, int which)
                { // TODO Auto-generated method stub
                    dialog.cancel();
                }
            });

            d.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


	/*
		if(view == R.id.mic){		//구글 음성인식 앱 사용이면
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);			//intent 생성
			i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());	//음성인식을 호출한 패키지
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");							//음성인식 언어 설정
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "말을 하세요.");						//사용자에게 보여 줄 글자

			startActivityForResult(i, GOOGLE_STT);		//구글 음성인식 실행
		}
		else if(view == R.id.connBtn)
		{
			if (cSocket == null) {
			logger("접속중입니다...");
	        connect(server, port , nickName);
			}
		}

		else if(view == R.id.closeBtn)
		{
			if (cSocket != null) {
			       sendMessage("# [" + nickName + "]님이 나가셨습니다.");
			      }
		}
		else if(view == R.id.closeBtn)
		{
			String msgString = msgText.getText().toString();
	          if (msgString != null && !"".equals(msgString)) {
	              sendMessage("[" + nickName + "] " + msgString);
	              msgText.setText("");
	             }
	      }
		else {
	       logger("접속을 먼저 해주세요.");
	      }


		//else if(view == R.id.hide){
		//	startActivityForResult(new Intent(this, CustomUIActivity.class), MY_UI);			//내가 만든 activity 실행
		//}
	}
	*/

    ///////////////////////음성인식 부분//////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if( resultCode == RESULT_OK  && (requestCode == GOOGLE_STT || requestCode == MY_UI) ){		//결과가 있으면
            showSelectDialog(requestCode, data);				//결과를 다이얼로그로 출력.
        }
        else{															//결과가 없으면 에러 메시지 출력
            String msg = null;

            //내가 만든 activity에서 넘어오는 오류 코드를 분류
            switch(resultCode){
                case SpeechRecognizer.ERROR_AUDIO:
                    msg = "오디오 입력 중 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    msg = "단말에서 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    msg = "권한이 없습니다.";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    msg = "네트워크 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    msg = "일치하는 항목이 없습니다.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    msg = "음성인식 서비스가 과부하 되었습니다.";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    msg = "서버에서 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    msg = "입력이 없습니다.";
                    break;
            }

            if(msg != null)		//오류 메시지가 null이 아니면 메시지 출력
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    //결과 list 출력하는 다이얼로그 생성
    private void showSelectDialog(int requestCode, Intent data){
        String key = "";
        if(requestCode == GOOGLE_STT)					//구글음성인식이면
            key = RecognizerIntent.EXTRA_RESULTS;	//키값 설정
        else if(requestCode == MY_UI)					//내가 만든 activity 이면
            key = SpeechRecognizer.RESULTS_RECOGNITION;	//키값 설정

        mResult = data.getStringArrayListExtra(key);		//인식된 데이터 list 받아옴.
        String[] result = new String[mResult.size()];			//배열생성. 다이얼로그에서 출력하기 위해
        mResult.toArray(result);									//	list 배열로 변환

        String[] Mesgon = {"불꺼","불 꺼", "불", "꺼", "불꺼어", "불꺼어어", "볼거", "볼 거", "볼 꺼"};

        //for(int i=0; i<=mResult.size(); i++)
        //{
        for(int j=0; j<=Mesgon.length-1; j++)
        {
            //if(mResult.equals(Mesgon[j]))
            if(mResult.contains(Mesgon[j]))		//음성인식된 문자와 불꺼 메시지 비교한다.
            {	//음성인식해서 지정한 단어 있으면
                Toast.makeText(getApplicationContext(), "찾았다!!!!", Toast.LENGTH_LONG).show();
                sendMessage("  =서버로 보낸다 : 불꺼=");
                break;
            }
        }
		/*
		//1개 선택하는 다이얼로그 생성
		AlertDialog ad = new AlertDialog.Builder(this).setTitle("선택하세요.")
							.setSingleChoiceItems(result, -1, new DialogInterface.OnClickListener() {
								@Override public void onClick(DialogInterface dialog, int which) {
										mSelectedString = mResult.get(which);		//선택하면 해당 글자 저장
								}
							})
							.setPositiveButton("확인", new DialogInterface.OnClickListener() {
								@Override public void onClick(DialogInterface dialog, int which) {
									mResultTextView.setText("인식결과 : "+mSelectedString);		//확인 버튼 누르면 결과 출력
									sendMessage(mSelectedString);		////서버로 전송
								}
							})
							.setNegativeButton("취소", new DialogInterface.OnClickListener() {
								@Override public void onClick(DialogInterface dialog, int which) {
									mResultTextView.setText("");		//취소버튼 누르면 초기화
									mSelectedString = null;
								}
							}).create();
		ad.show();
		*/
    }


    /////////////////////////네트워크 전송///////////////////////////////
	/*

	    public void connBtnClick(View v) {
	     switch (v.getId()) {
	     case R.id.connBtn: // 접속버튼
	      if (cSocket == null) {

	          logger("접속중입니다...");
	          connect(server, port , nickName);
	         }
	      break;
	     case R.id.closeBtn: // 나가기 버튼
	      if (cSocket != null) {
	       sendMessage("# [" + nickName + "]님이 나가셨습니다.");
	      }
	     break;
	     case R.id.sendBtn: // 메세지 보내기 버튼
	      if (cSocket != null) {
	       String msgString = msgText.getText().toString();
	          if (msgString != null && !"".equals(msgString)) {
	              sendMessage("[" + nickName + "] " + msgString);
	              msgText.setText("");
	             }
	      } else {
	       logger("접속을 먼저 해주세요.");
	      }
	     break;
	     }
	    }
	*/

    private class connectTask extends AsyncTask<String, Void , Socket> {

        @Override
        protected Socket doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                cSocket = new Socket(server, port);
                streamOut = new PrintWriter(cSocket.getOutputStream(), true);
                streamIn = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            sendMessage("# 새로운 이용자님이 들어왔습니다.");

            cThread = new chatThread();
            cThread.start();

            return null;
        }

    }


    public void connect(String server, int port, String user) {
        System.out.println("커넥트 시작");
        new connectTask().execute(server);
    }


    public void onDestroy() { // 앱이 소멸되면
        super.onDestroy();
        if (cSocket != null) {
            //sendMessage("# [" + nickName + "]님이 나가셨습니다.");
        }
    }


    private void logger(String MSG) {
        tv.append(MSG + "\n");     // 텍스트뷰에 메세지를 더해줍니다.
        sv.fullScroll(ScrollView.FOCUS_DOWN); // 스크롤뷰의 스크롤을 내려줍니다.
    }

    private void sendMessage(String MSG) {
        try {
            streamOut.println(MSG);     // 서버에 메세지를 보내줍니다.
        } catch (Exception ex) {
            logger(ex.toString());
        }

    }
    Handler mHandler = new Handler() {   // 스레드에서 메세지를 받을 핸들러.
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // 채팅 메세지를 받아온다.
                    logger(msg.obj.toString());
                    break;
                case 1: // 소켓접속을 끊는다.
                    try {
                        cSocket.close();
                        cSocket = null;

                        logger("접속이 끊어졌습니다.");

                    } catch (Exception e) {
                        logger("접속이 이미 끊겨 있습니다." + e.getMessage());
                        finish();
                    }
                    break;
            }
        }
    };


    class chatThread extends Thread {
        private boolean flag = false; // 스레드 유지(종료)용 플래그
        public void run() {
            try {
                while (!flag) { // 플래그가 false일경우에 루프
                    String msgs;
                    Message msg = new Message();
                    msg.what = 0;
                    msgs = streamIn.readLine();  // 서버에서 올 메세지를 기다린다.
                    msg.obj = msgs;

                    mHandler.sendMessage(msg); // 핸들러로 메세지 전송

                    if (msgs.equals("# [" + nickName + "]님이 나가셨습니다.")) { // 서버에서 온 메세지가 종료 메세지라면
                        flag = true;   // 스레드 종료를 위해 플래그를 true로 바꿈.
                        msg = new Message();
                        msg.what = 1;   // 종료메세지
                        mHandler.sendMessage(msg);
                    }
                }

            }catch(Exception e) {
                logger(e.getMessage());
            }
        }
    }





}
/*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
*/