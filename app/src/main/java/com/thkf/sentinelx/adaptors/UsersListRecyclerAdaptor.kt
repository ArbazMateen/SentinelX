package com.thkf.sentinelx.adaptors

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import com.thkf.sentinelx.R
import com.thkf.sentinelx.commons.ONLINE
import com.thkf.sentinelx.commons.timeDifference
import com.thkf.sentinelx.models.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_users_list.view.*


interface RecyclerItemClickListener {
    fun onRecyclerItemClick(item: User, pos: Int)
}

interface RecyclerListMenuItemClickListener {
    fun onFollow(item: User, pos: Int)
    fun onUnFollow(item: User, pos: Int)
}

class UsersListRecyclerAdaptor(val context: Context, private var usersList: List<User>) :
        RecyclerView.Adapter<UsersListRecyclerAdaptor.Holder>() {

    private var listener: RecyclerItemClickListener? = null
    private var menuItemListener: RecyclerListMenuItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(context, this,
                LayoutInflater.from(context)
                        .inflate(R.layout.item_users_list, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(usersList[position], position)
    }

    override fun getItemCount() = usersList.size

    fun changeData(data: List<User>) {
        this.usersList = data
        notifyDataSetChanged()
    }

    fun onClick(pos: Int) {
        if(context is RecyclerItemClickListener) {
            listener = context
            if(listener != null) {
                listener?.onRecyclerItemClick(usersList[pos], pos)
            }
        }
    }

    fun follow(user: User, pos: Int) {
        if(context is RecyclerListMenuItemClickListener) {
            menuItemListener = context
            if(menuItemListener != null) {
                menuItemListener?.onFollow(user, pos)
            }
        }
    }

    fun unFollow(user: User, pos: Int) {
        if(context is RecyclerListMenuItemClickListener) {
            menuItemListener = context
            if(menuItemListener != null) {
                menuItemListener?.onUnFollow(user, pos)
            }
        }
    }

    class Holder(val context: Context, private val adaptor: UsersListRecyclerAdaptor, view: View) :
            RecyclerView.ViewHolder(view), View.OnClickListener {

        private val profileImage: CircleImageView = view.user_profile_image
        private val status: ImageView = view.user_status
        private val username: TextView = view.user_display_name
        private val email: TextView = view.user_email
//        private val optionMenu: ImageView = view.user_option_menu

        private lateinit var user: User
        private var pos = -1

        init {
            view.setOnClickListener(this)
//            optionMenu.setOnClickListener {
//                val popUpMenu = PopupMenu(context, it)
//                popUpMenu.inflate(R.menu.menu_user_list_item)
//                popUpMenu.setOnMenuItemClickListener {
//                    when(it.itemId) {
//                        R.id.follow -> {
//                            localLogI("${user.lat} ${user.lon}")
//
//                            adaptor.follow(user, pos)
//
//                            true
//                        }
//                        R.id.un_follow -> {
//                            adaptor.unFollow(user, pos)
//                            true
//                        }
//                        else -> {
//                            false
//                        }
//                    }
//                }
//                popUpMenu.show()
//            }

        }

        override fun onClick(p0: View?) {
            adaptor.onClick(pos)
        }

        fun bind(user: User, p: Int) {
            this.user = user
            this.pos = p
            username.text = user.name

            ImageLoader.getInstance().displayImage(user.image, profileImage)

            if(user.status == ONLINE) {
                status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.online))
                email.text = user.email
            }
            else {
                status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.offline))
                email.text = timeDifference(user.last_update)
            }

        }

    }

}
