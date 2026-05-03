package com.example.binorwin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.binorwin.model.Argument
import com.example.binorwin.model.Post
import com.example.binorwin.ui.theme.BinOrWinTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinOrWinTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    // State to control Bottom Sheet visibility and which post is currently selected
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<Int?>(null) }

    // Controls the animation and state of the bottom sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bin Or Win Feed") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.refreshPosts() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No posts yet. Pull to refresh or check server connection.")
                }
            } else {
                PostList(
                    posts = viewModel.posts,
                    onVote = { postId, type -> viewModel.vote(postId, type) },
                    onDiscussClick = { postId ->
                        selectedPostId = postId
                        viewModel.fetchArguments(postId)
                        showBottomSheet = true
                    }
                )
            }
        }
    }

    // Display the Bottom Sheet if the state is true
    if (showBottomSheet && selectedPostId != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.85f) // Take up 85% of the screen height
        ) {
            ArgumentBottomSheetContent(
                postId = selectedPostId!!,
                arguments = viewModel.currentArguments,
                onSubmit = { actionType, content ->
                    viewModel.createArgument(selectedPostId!!, actionType, content)
                }
            )
        }
    }
}

@Composable
fun PostList(posts: List<Post>, onVote: (Int, String) -> Unit, onDiscussClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            PostCard(post = post, onVote = onVote, onDiscussClick = { onDiscussClick(post.id) })
        }
    }
}

@Composable
fun PostCard(post: Post, onVote: (Int, String) -> Unit, onDiscussClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onVote(post.id, "bin") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Bin (${post.binVotes})")
                }

                Button(
                    onClick = { onVote(post.id, "win") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Win (${post.winVotes})")
                }
            }

            // Discuss Button to open the bottom sheet
            TextButton(
                onClick = onDiscussClick,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            ) {
                Text("View Arguments & Discuss")
            }
        }
    }
}

@Composable
fun ArgumentBottomSheetContent(
    postId: Int,
    arguments: List<Argument>,
    onSubmit: (String, String) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    // Default action type for the argument (user can toggle this before posting)
    var selectedActionType by remember { mutableStateOf("win") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Arguments",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // List of existing arguments
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (arguments.isEmpty()) {
                item {
                    Text("No arguments yet. Be the first to discuss!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(arguments) { arg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (arg.actionType == "win")
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (arg.actionType == "win") "WIN Argument" else "BIN Argument",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (arg.actionType == "win")
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = arg.content)
                        }
                    }
                }
            }
        }

        // Input area for new argument
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // A simple toggle to choose whether the argument supports BIN or WIN
            TextButton(
                onClick = { selectedActionType = if (selectedActionType == "win") "bin" else "win" },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (selectedActionType == "win") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) {
                Text(selectedActionType.uppercase())
            }

            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                placeholder = { Text("Write your argument...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (contentText.isNotBlank()) {
                        onSubmit(selectedActionType, contentText)
                        contentText = "" // Clear the input field after submitting
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Argument")
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Padding for bottom system navigation bar
    }
}