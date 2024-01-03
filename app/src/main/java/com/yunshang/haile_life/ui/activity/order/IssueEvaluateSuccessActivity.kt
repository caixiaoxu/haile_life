package com.yunshang.haile_life.ui.activity.order

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lsy.framelib.ui.base.activity.BaseBindingActivity
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.ActivityIssueEvaluateSuccessBinding

class IssueEvaluateSuccessActivity : BaseBindingActivity<ActivityIssueEvaluateSuccessBinding>() {

    override fun layoutId(): Int = R.layout.activity_issue_evaluate_success

    override fun backBtn(): View = mBinding.barIssueEvaluateSuccessTitle.getBackBtn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.WHITE

        mBinding.cvIssueEvaluateSuccessContent.setContent {
            IssueEvaluateContent()
        }
    }

    @Preview
    @Composable
    fun IssueEvaluateContent() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.mipmap.icon_issue_evaluate_success_main),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.evaluate_success),
                fontSize = 17.sp,
                color = colorResource(id = R.color.color_black_85)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.evaluate_success_prompt),
                fontSize = 14.sp,
                color = colorResource(id = R.color.color_black_85)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    finish()
                },
                Modifier.size(98.dp, 36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
                border = BorderStroke(1.dp, colorResource(id = R.color.color_cccccc))
            ) {
                Text(
                    text = stringResource(id = R.string.back), fontSize = 14.sp,
                    color = colorResource(id = R.color.color_black_85)
                )
            }
        }
    }
}